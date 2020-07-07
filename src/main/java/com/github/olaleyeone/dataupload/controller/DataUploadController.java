package com.github.olaleyeone.dataupload.controller;

import com.github.olaleyeone.dataupload.data.dto.DataUploadApiRequest;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.repository.DataUploadRepository;
import com.github.olaleyeone.dataupload.response.handler.DataUploadApiResponseHandler;
import com.github.olaleyeone.dataupload.response.pojo.DataUploadApiResponse;
import com.github.olaleyeone.dataupload.service.api.DataUploadService;
import com.github.olaleyeone.rest.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
public class DataUploadController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final DataUploadService dataUploadService;
    private final DataUploadRepository dataUploadRepository;
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
}
