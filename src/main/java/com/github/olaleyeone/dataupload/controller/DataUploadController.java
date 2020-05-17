package com.github.olaleyeone.dataupload.controller;

import com.github.olaleyeone.dataupload.data.dto.DataUploadApiRequest;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.response.pojo.DataUploadApiResponse;
import com.github.olaleyeone.dataupload.service.api.DataUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
public class DataUploadController {

    private final DataUploadService dataUploadService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/uploads")
    public DataUploadApiResponse createDataUpload(@RequestBody @Valid DataUploadApiRequest apiRequest) {
        DataUpload dataUpload = dataUploadService.createDataUpload(apiRequest);
        return new DataUploadApiResponse(dataUpload);
    }
}
