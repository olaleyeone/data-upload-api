package com.github.olaleyeone.dataupload.response.pojo;

import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DataUploadApiResponse {

    private Long id;

    private String contentType;

    private Long size;

    private String description;

    private Long sizeUploaded;

    private List<MissingChunkApiResponse> missingChunks;

    public DataUploadApiResponse(DataUpload dataUpload) {
        this.id = dataUpload.getId();
        this.contentType = dataUpload.getContentType();
        this.size = dataUpload.getSize();
        this.description = dataUpload.getDescription();
    }
}
