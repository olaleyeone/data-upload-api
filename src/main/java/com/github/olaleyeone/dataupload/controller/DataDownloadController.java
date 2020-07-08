package com.github.olaleyeone.dataupload.controller;

import com.github.olaleyeone.auth.annotations.Public;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.repository.DataUploadChunkRepository;
import com.github.olaleyeone.dataupload.repository.DataUploadRepository;
import com.github.olaleyeone.dataupload.response.handler.FullDataResponseHandler;
import com.github.olaleyeone.dataupload.response.handler.RangeResponseHandler;
import com.github.olaleyeone.rest.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class DataDownloadController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final DataUploadRepository dataUploadRepository;
    private final DataUploadChunkRepository dataUploadChunkRepository;

    private final FullDataResponseHandler fullDataResponseHandler;
    private final RangeResponseHandler rangeResponseHandler;

    @Public
    @GetMapping("/uploads/{id:\\d+}/data")
    public void getData(
            @PathVariable("id") Long dataUploadId,
            HttpServletResponse httpServletResponse) throws IOException {
        DataUpload dataUpload = getDataUpload(dataUploadId);
        fullDataResponseHandler.sendAll(dataUpload, httpServletResponse);
    }

    @Public
    @GetMapping(path = "/uploads/{id:\\d+}/data", headers = "Range")
    public ResponseEntity<?> getDataRange(
            @PathVariable("id") Long dataUploadId,
            HttpServletResponse httpServletResponse,
            @RequestHeader HttpHeaders httpHeaders) throws IOException {
        DataUpload dataUpload = getDataUpload(dataUploadId);
        List<HttpRange> ranges = httpHeaders.getRange();
        HttpRange range = ranges.iterator().next();
        if (rangeEqualsAll(range)) {
            fullDataResponseHandler.sendAll(dataUpload, httpServletResponse);
            return null;
        }
        return rangeResponseHandler.sendRange(dataUpload, ranges);
    }

    private boolean rangeEqualsAll(HttpRange range) {
        return range.getRangeStart(Long.MAX_VALUE) == 0 && range.getRangeEnd(Long.MAX_VALUE) >= Long.MAX_VALUE - 1;
    }

    private DataUpload getDataUpload(@PathVariable("id") Long dataUploadId) {
        DataUpload dataUpload = dataUploadRepository.findById(dataUploadId)
                .orElseThrow(NotFoundException::new);
        if (dataUpload.getSize() == null) {
            throw new NotFoundException();
        }
        if (!dataUpload.getSize().equals(dataUploadChunkRepository.sumData(dataUpload))) {
            throw new NotFoundException();
        }
        return dataUpload;
    }
}
