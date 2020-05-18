package com.github.olaleyeone.dataupload.controller;

import com.github.olaleyeone.dataupload.data.dto.DataUploadApiRequest;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.repository.DataUploadChunkRepository;
import com.github.olaleyeone.dataupload.repository.DataUploadRepository;
import com.github.olaleyeone.dataupload.response.handler.DataUploadApiResponseHandler;
import com.github.olaleyeone.dataupload.response.pojo.DataUploadApiResponse;
import com.github.olaleyeone.dataupload.service.api.DataUploadService;
import com.olaleyeone.rest.exception.ErrorResponse;
import com.olaleyeone.rest.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class DataUploadController {

    private final DataUploadService dataUploadService;
    private final DataUploadRepository dataUploadRepository;
    private final DataUploadChunkRepository dataUploadChunkRepository;
    private final DataUploadApiResponseHandler dataUploadApiResponseHandler;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/uploads")
    public DataUploadApiResponse createDataUpload(@RequestBody @Valid DataUploadApiRequest apiRequest) {
        DataUpload dataUpload = dataUploadService.createDataUpload(apiRequest);
        return new DataUploadApiResponse(dataUpload);
    }

    @GetMapping("/uploads/{id:\\d+}")
    public DataUploadApiResponse getDetails(@PathVariable("id") Long dataUploadId) {
        DataUpload dataUpload = dataUploadRepository.findById(dataUploadId)
                .orElseThrow(NotFoundException::new);
        return dataUploadApiResponseHandler.getDataUploadApiResponse(dataUpload);
    }

    @GetMapping("/uploads/{id:\\d+}/data")
    public void getData(
            @PathVariable("id") Long dataUploadId,
            HttpServletResponse httpServletResponse) {
        DataUpload dataUpload = dataUploadRepository.findById(dataUploadId)
                .orElseThrow(NotFoundException::new);
        if (dataUpload.getSize() == null) {
            throw new NotFoundException();
        }
        if (!dataUpload.getSize().equals(dataUploadChunkRepository.sumData(dataUpload))) {
            throw new NotFoundException();
        }
        httpServletResponse.setContentLength(dataUpload.getSize().intValue());
        httpServletResponse.setContentType(dataUpload.getContentType());
        List<Long> chunkIds = dataUploadChunkRepository.getChunkIds(dataUpload);
        for (Long id : chunkIds) {
            dataUploadChunkRepository.findById(id).ifPresent(dataUploadChunk -> {
                try {
                    httpServletResponse.getOutputStream().write(dataUploadChunk.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            });
        }
    }
}
