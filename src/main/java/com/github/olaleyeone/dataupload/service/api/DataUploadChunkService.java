package com.github.olaleyeone.dataupload.service.api;

import com.github.olaleyeone.dataupload.data.dto.DataUploadChunkApiRequest;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.data.entity.DataUploadChunk;

public interface DataUploadChunkService {

    DataUploadChunk createChunk(DataUpload dataUpload, DataUploadChunkApiRequest dto);

    void delete(DataUploadChunk dataUploadChunk);
}
