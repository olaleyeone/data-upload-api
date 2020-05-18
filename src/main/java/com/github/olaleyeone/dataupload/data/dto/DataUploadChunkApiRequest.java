package com.github.olaleyeone.dataupload.data.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class DataUploadChunkApiRequest {

    @NotNull
    @Min(1)
    private Long start;

    @NotNull
    @Size(min = 1)
    private byte[] data;

    @NotBlank
    private String contentType;

    private Long totalSize;
}
