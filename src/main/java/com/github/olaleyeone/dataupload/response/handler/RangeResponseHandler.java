package com.github.olaleyeone.dataupload.response.handler;

import com.github.olaleyeone.dataupload.data.dto.DataChunk;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.repository.DataUploadChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
public class RangeResponseHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final DataUploadChunkRepository dataUploadChunkRepository;

    @SneakyThrows
    public ResponseEntity<ByteArrayResource> sendRange(DataUpload dataUpload, List<HttpRange> ranges) {

        HttpRange firstRange = ranges.iterator().next();

        long start = firstRange.getRangeStart(dataUpload.getSize());
        long end = firstRange.getRangeEnd(dataUpload.getSize());

        long contentLength = (end - start) + 1;
        List<DataChunk> chunks = dataUploadChunkRepository.getChunks(dataUpload, start + 1, end + 1);
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
            responseHeaders.setContentType(MediaType.valueOf(dataUpload.getContentType()));
            responseHeaders.setContentLength(byteArrayOutputStream.size());

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .headers(responseHeaders)
                    .body(new ByteArrayResource(byteArrayOutputStream.toByteArray()));
        }
    }
}
