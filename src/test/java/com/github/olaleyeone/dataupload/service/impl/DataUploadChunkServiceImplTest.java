package com.github.olaleyeone.dataupload.service.impl;

import com.github.olaleyeone.dataupload.data.dto.DataUploadChunkApiRequest;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.data.entity.DataUploadChunk;
import com.github.olaleyeone.dataupload.service.api.DataUploadChunkService;
import com.github.olaleyeone.dataupload.test.service.ServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class DataUploadChunkServiceImplTest extends ServiceTest {

    @Autowired
    private DataUploadChunkService dataUploadChunkService;

    private DataUpload dataUpload;

    @BeforeEach
    public void setUp() {
        dataUpload = modelFactory.create(DataUpload.class);
    }

    @Test
    void createChunk() {
        DataUploadChunkApiRequest apiRequest = dtoFactory.make(DataUploadChunkApiRequest.class);
        DataUploadChunk dataUploadChunk = dataUploadChunkService.createChunk(dataUpload, apiRequest);
        assertNotNull(dataUploadChunk);
        assertNotNull(dataUploadChunk.getId());
        assertEquals(dataUpload, dataUploadChunk.getDataUpload());
        assertEquals(apiRequest.getStart(), dataUploadChunk.getStart());
        assertEquals(apiRequest.getData().length, dataUploadChunk.getSize());
        assertArrayEquals(apiRequest.getData(), dataUploadChunk.getData());
    }

    @Test
    void deleteRecord() {
        DataUploadChunk dataUploadChunk = modelFactory.create(DataUploadChunk.class);
        dataUploadChunkService.delete(dataUploadChunk);
        entityManager.flush();
        assertNull(entityManager.find(DataUploadChunk.class, dataUploadChunk.getId()));
    }
}