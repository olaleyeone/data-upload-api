package com.github.olaleyeone.dataupload.controller;

import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.repository.DataUploadChunkRepository;
import com.github.olaleyeone.dataupload.repository.DataUploadRepository;
import com.github.olaleyeone.dataupload.response.handler.FullDataResponseHandler;
import com.github.olaleyeone.dataupload.response.handler.RangeResponseHandler;
import com.github.olaleyeone.dataupload.test.controller.ControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DataDownloadControllerTest extends ControllerTest {

    @Autowired
    private DataUploadRepository dataUploadRepository;
    @Autowired
    private DataUploadChunkRepository dataUploadChunkRepository;

    @Autowired
    private FullDataResponseHandler fullDataResponseHandler;
    @Autowired
    private RangeResponseHandler rangeResponseHandler;

    private DataUpload dataUpload;

    @BeforeEach
    void setUp() {
        dataUpload = new DataUpload();
    }

    @Test
    public void getDataForFreshRecord() throws Exception {
        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}/data", faker.number().randomDigit()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getDataForIncompleteUpload() throws Exception {
        dataUpload.setSize(faker.number().randomNumber());
        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());
        Mockito.doReturn(dataUpload.getSize() / 2).when(dataUploadChunkRepository).sumData(Mockito.any());
        long id = faker.number().randomDigit();
        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}/data", id))
                .andExpect(status().isNotFound());
        Mockito.verify(dataUploadRepository, Mockito.times(1))
                .findById(id);
        Mockito.verify(dataUploadChunkRepository, Mockito.times(1))
                .sumData(dataUpload);
    }

    @Test
    public void getData() throws Exception {
        dataUpload.setSize(faker.number().randomNumber());
        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());
        Mockito.doReturn(dataUpload.getSize()).when(dataUploadChunkRepository).sumData(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}/data", faker.number().randomDigit()))
                .andExpect(status().isOk());
        Mockito.verify(fullDataResponseHandler, Mockito.times(1)).sendAll(Mockito.eq(dataUpload), Mockito.any());
    }

    @Test
    public void getDataForRange() throws Exception {
        dataUpload.setSize(faker.number().randomNumber());
        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());
        Mockito.doReturn(dataUpload.getSize()).when(dataUploadChunkRepository).sumData(Mockito.any());

        HttpHeaders httpHeaders = new HttpHeaders();
        List<HttpRange> ranges = Collections.singletonList(HttpRange.createByteRange(0, 100));
        httpHeaders.setRange(ranges);
        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}/data", faker.number().randomDigit())
                .headers(httpHeaders))
                .andExpect(status().isOk());
        Mockito.verify(rangeResponseHandler, Mockito.times(1)).sendRange(dataUpload, ranges);
    }
}