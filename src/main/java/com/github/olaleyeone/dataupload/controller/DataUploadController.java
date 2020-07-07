package com.github.olaleyeone.dataupload.controller;

import com.github.olaleyeone.auth.annotations.Public;
import com.github.olaleyeone.dataupload.data.dto.DataChunk;
import com.github.olaleyeone.dataupload.data.dto.DataUploadApiRequest;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.data.entity.DataUploadChunk;
import com.github.olaleyeone.dataupload.repository.DataUploadChunkRepository;
import com.github.olaleyeone.dataupload.repository.DataUploadRepository;
import com.github.olaleyeone.dataupload.response.handler.DataUploadApiResponseHandler;
import com.github.olaleyeone.dataupload.response.pojo.DataUploadApiResponse;
import com.github.olaleyeone.dataupload.service.api.DataUploadService;
import com.github.olaleyeone.rest.exception.ErrorResponse;
import com.github.olaleyeone.rest.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class DataUploadController {

    private Logger logger = LoggerFactory.getLogger(getClass());

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

    @Public
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
            DataUploadChunk dataUploadChunk = dataUploadChunkRepository.findById(id)
                    .orElseThrow(() -> new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR));
            try {
                httpServletResponse.getOutputStream().write(dataUploadChunk.getData());
            } catch (IOException e) {
                e.printStackTrace();
                throw new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Public
    @GetMapping(path = "/uploads/{id:\\d+}/data", headers = "Range")
    public ResponseEntity<?> getDataRange(
            @PathVariable("id") Long dataUploadId,
            @RequestHeader HttpHeaders httpHeaders) throws IOException {
        logger.info("Headers: {}", httpHeaders);
        DataUpload dataUpload = dataUploadRepository.findById(dataUploadId)
                .orElseThrow(NotFoundException::new);
        if (dataUpload.getSize() == null) {
            throw new NotFoundException();
        }
        if (!dataUpload.getSize().equals(dataUploadChunkRepository.sumData(dataUpload))) {
            throw new NotFoundException();
        }

        List<HttpRange> range = httpHeaders.getRange();
        HttpRange firstRange = range.iterator().next();

        long start = firstRange.getRangeStart(dataUpload.getSize());
        long end = firstRange.getRangeEnd(dataUpload.getSize());

        long contentLength = (end - start) + 1;
//        logger.info("{}-{} [{}]", start, end, contentLength);
        List<DataChunk> chunks = dataUploadChunkRepository.getChunks(dataUpload, start + 1, end + 1);
//        List<DataChunk> chunks = dataUploadChunkRepository.getChunks(dataUpload);
        logger.info("Chunks: {}", chunks);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            for (DataChunk dataChunk : chunks) {
                int offset = Long.valueOf(byteArrayOutputStream.size() + start).intValue();

                int offsetInChunk = offset - Long.valueOf(dataChunk.getStart() - 1).intValue();
                int bytesLeftInChunk = Math.max(0, dataChunk.getSize() - offsetInChunk);
                if (bytesLeftInChunk == 0) {
                    continue;
                }

                int bytesToWrite = Math.min(Long.valueOf(contentLength - byteArrayOutputStream.size()).intValue(), bytesLeftInChunk);

                byte[] data = dataUploadChunkRepository.getData(dataChunk.getId(), offsetInChunk, bytesToWrite);
//                logger.info("{}[{}]: {}-{} [{}]", dataUploadChunk.getStart() - 1,
//                        dataUploadChunk.getSize(),
//                        offsetInChunk,
//                        offsetInChunk + bytesToWrite,
//                        bytesToWrite);
                byteArrayOutputStream.write(data);
                if (byteArrayOutputStream.size() >= contentLength) {
                    break;
                }
            }

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setRange(Collections.singletonList(HttpRange.createByteRange(start, end)));
            responseHeaders.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            responseHeaders.set(
                    HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d", start, end, dataUpload.getSize()));

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .headers(responseHeaders)
                    .contentLength(byteArrayOutputStream.size())
                    .contentType(MediaType.valueOf(dataUpload.getContentType()))
                    .body(new ByteArrayResource(byteArrayOutputStream.toByteArray()));
        }
    }
}
