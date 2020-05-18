package com.github.olaleyeone.dataupload.controller;

import com.github.olaleyeone.dataupload.data.dto.DataUploadChunkApiRequest;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.data.entity.DataUploadChunk;
import com.github.olaleyeone.dataupload.event.mesage.UploadCompleteEvent;
import com.github.olaleyeone.dataupload.repository.DataUploadChunkRepository;
import com.github.olaleyeone.dataupload.repository.DataUploadRepository;
import com.github.olaleyeone.dataupload.response.handler.DataUploadApiResponseHandler;
import com.github.olaleyeone.dataupload.response.pojo.DataUploadApiResponse;
import com.github.olaleyeone.dataupload.service.api.DataUploadChunkService;
import com.olaleyeone.rest.exception.ErrorResponse;
import com.olaleyeone.rest.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RequiredArgsConstructor
@RestController
public class DataUploadChunkController {

    private final DataUploadRepository dataUploadRepository;
    private final DataUploadChunkRepository dataUploadChunkRepository;
    private final DataUploadChunkService dataUploadChunkService;
    private final DataUploadApiResponseHandler dataUploadApiResponseHandler;
    private final ApplicationContext applicationContext;

    @PostMapping("/uploads/{id:\\d+}/data/{start:\\d+}")
    public DataUploadApiResponse uploadChunk(
            @PathVariable("id") Long dataUploadId,
            @PathVariable("start") Long start,
            @RequestParam(value = "totalSize", required = false) Long totalSize,
            HttpServletRequest request) throws IOException {
        if (start == 0) {
            throw new ErrorResponse(HttpStatus.BAD_REQUEST, "Start must be 1 or more");
        }
        DataUpload dataUpload = getDataUpload(dataUploadId, request);
        DataUploadChunkApiRequest apiRequest = getDataUploadChunkApiRequest(start, request, dataUpload);

        if (dataUpload.getSize() == null) {
            if (totalSize == null || totalSize < 1) {
                throw new ErrorResponse(HttpStatus.BAD_REQUEST, "Valid total size required");
            }
            apiRequest.setTotalSize(totalSize);
        } else if (dataUpload.getSize() < (apiRequest.getStart() + apiRequest.getData().length) - 1) {
            throw new ErrorResponse(HttpStatus.BAD_REQUEST, String.format("Cannot write beyond expected upload size %d",
                    dataUpload.getSize()));
        }

        DataUploadChunk chunk = dataUploadChunkService.createChunk(dataUpload, apiRequest);
        if (countChunkInRange(dataUpload, apiRequest) > 1) {
            dataUploadChunkService.delete(chunk);
            throw new ErrorResponse(HttpStatus.BAD_REQUEST, "Data already in range");
        }
        DataUploadApiResponse dataUploadApiResponse = dataUploadApiResponseHandler.getDataUploadApiResponse(dataUpload);
        if (dataUpload.getSize().equals(dataUploadApiResponse.getSizeUploaded())) {
            applicationContext.publishEvent(new UploadCompleteEvent(dataUpload));
        }
        return dataUploadApiResponse;
    }

    private DataUpload getDataUpload(Long dataUploadId, HttpServletRequest request) {
        DataUpload dataUpload = dataUploadRepository.findById(dataUploadId)
                .orElseThrow(NotFoundException::new);
        if (StringUtils.isNotBlank(dataUpload.getContentType()) && !dataUpload.getContentType().equals(request.getContentType())) {
            throw new ErrorResponse(HttpStatus.BAD_REQUEST, String.format("Expected content of type %s but received %s",
                    dataUpload.getContentType(), request.getContentType()));
        }
        return dataUpload;
    }

    private DataUploadChunkApiRequest getDataUploadChunkApiRequest(Long start, HttpServletRequest request, DataUpload dataUpload) throws IOException {
        DataUploadChunkApiRequest apiRequest = new DataUploadChunkApiRequest();
        apiRequest.setStart(start);
        apiRequest.setContentType(request.getContentType());
        apiRequest.setData(StreamUtils.copyToByteArray(request.getInputStream()));
        if (countChunkInRange(dataUpload, apiRequest) > 0) {
            throw new ErrorResponse(HttpStatus.BAD_REQUEST, "Data already in range");
        }
        return apiRequest;
    }

    private int countChunkInRange(DataUpload dataUpload, DataUploadChunkApiRequest apiRequest) {
        return dataUploadChunkRepository.countByRange(dataUpload, apiRequest.getStart(), apiRequest.getData().length);
    }
}
