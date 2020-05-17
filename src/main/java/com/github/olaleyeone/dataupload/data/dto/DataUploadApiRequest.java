package com.github.olaleyeone.dataupload.data.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DataUploadApiRequest {

//    @NotBlank
//    private String contentType;

    @NotNull
    @Min(1)
    private Long size;

    private String description;

    @NotBlank
    private String userId;
}
