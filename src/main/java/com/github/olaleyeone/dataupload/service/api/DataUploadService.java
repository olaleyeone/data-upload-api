package com.github.olaleyeone.dataupload.service.api;

import com.github.olaleyeone.dataupload.data.dto.DataUploadApiRequest;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;

public interface DataUploadService {

    DataUpload createDataUpload(DataUploadApiRequest dto);
}
