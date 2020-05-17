package com.github.olaleyeone.dataupload.service.impl;

import com.github.olaleyeone.dataupload.data.dto.DataUploadApiRequest;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.service.api.DataUploadService;
import com.github.olaleyeone.dataupload.test.service.ServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DataUploadServiceImplTest extends ServiceTest {

    @Autowired
    private DataUploadService dataUploadService;

    @Test
    void createDataUpload() {
        DataUploadApiRequest apiRequest = dtoFactory.make(DataUploadApiRequest.class);
        DataUpload dataUpload = dataUploadService.createDataUpload(apiRequest);
        assertNotNull(dataUpload);
        assertNotNull(dataUpload.getId());
//        assertEquals(apiRequest.getContentType(), dataUpload.getContentType());
        assertEquals(apiRequest.getDescription(), dataUpload.getDescription());
        assertEquals(apiRequest.getSize(), dataUpload.getSize());
        assertEquals(apiRequest.getUserId(), dataUpload.getUserId());
    }
}