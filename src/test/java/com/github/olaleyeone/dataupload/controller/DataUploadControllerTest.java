package com.github.olaleyeone.dataupload.controller;

import com.github.olaleyeone.dataupload.data.dto.DataUploadApiRequest;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.data.entity.DataUploadChunk;
import com.github.olaleyeone.dataupload.repository.DataUploadChunkRepository;
import com.github.olaleyeone.dataupload.repository.DataUploadRepository;
import com.github.olaleyeone.dataupload.response.handler.DataUploadApiResponseHandler;
import com.github.olaleyeone.dataupload.response.pojo.DataUploadApiResponse;
import com.github.olaleyeone.dataupload.service.api.DataUploadService;
import com.github.olaleyeone.dataupload.test.controller.ControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DataUploadControllerTest extends ControllerTest {

    @Autowired
    private DataUploadService dataUploadService;

    @Autowired
    private DataUploadRepository dataUploadRepository;

    @Autowired
    private DataUploadChunkRepository dataUploadChunkRepository;

    @Autowired
    private DataUploadApiResponseHandler dataUploadApiResponseHandler;

    @Test
    void createDataUpload() throws Exception {
        DataUpload dataUpload = new DataUpload();
        dataUpload.setId(faker.number().randomNumber());
        Mockito.doReturn(dataUpload).when(dataUploadService).createDataUpload(Mockito.any());
        DataUploadApiRequest apiRequest = dtoFactory.make(DataUploadApiRequest.class);
        mockMvc.perform(MockMvcRequestBuilders.post("/uploads")
                .with(body(apiRequest)))
                .andExpect(status().isCreated())
                .andExpect(result -> {
                    DataUploadApiResponse apiResponse = objectMapper.readValue(result.getResponse().getContentAsByteArray(), DataUploadApiResponse.class);
                    assertNotNull(apiResponse);
                    assertEquals(dataUpload.getId(), apiResponse.getId());
                });

        Mockito.verify(dataUploadService, Mockito.times(1))
                .createDataUpload(apiRequest);
    }

    @Test
    public void getDetails() throws Exception {
        DataUpload dataUpload = new DataUpload();
        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());
        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}", faker.number().randomDigit()))
                .andExpect(status().isOk());
        Mockito.verify(dataUploadApiResponseHandler, Mockito.times(1))
                .getDataUploadApiResponse(dataUpload);
    }

    @Test
    public void handleDetailsNotFound() throws Exception {
        Mockito.doReturn(Optional.empty()).when(dataUploadRepository).findById(Mockito.any());
        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}", faker.number().randomDigit()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getDataForFreshRecord() throws Exception {
        DataUpload dataUpload = new DataUpload();
        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());
        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}/data", faker.number().randomDigit()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getDataForIncompleteUpload() throws Exception {
        DataUpload dataUpload = new DataUpload();
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
        DataUpload dataUpload = new DataUpload();
        dataUpload.setSize(faker.number().randomNumber());
        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());
        Mockito.doReturn(dataUpload.getSize()).when(dataUploadChunkRepository).sumData(Mockito.any());

        long start = faker.number().randomDigit();
        List<Long> ids = Arrays.asList(start++, start++, start++);
        Mockito.doReturn(ids).when(dataUploadChunkRepository).getChunkIds(Mockito.any());
        List<DataUploadChunk> uploadChunks = getChunks(ids.size());
        Mockito.doAnswer(invocation -> Optional.of(uploadChunks.get(ids.indexOf(invocation.getArgument(0)))))
                .when(dataUploadChunkRepository).findById(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}/data", faker.number().randomDigit()))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    uploadChunks.forEach(dataUploadChunk -> {
                        try {
                            bos.write(dataUploadChunk.getData());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    assertArrayEquals(bos.toByteArray(), result.getResponse().getContentAsByteArray());
                });
    }

    @Test
    public void getDataWithMissingChunk() throws Exception {
        DataUpload dataUpload = new DataUpload();
        dataUpload.setSize(faker.number().randomNumber());
        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());
        Mockito.doReturn(dataUpload.getSize()).when(dataUploadChunkRepository).sumData(Mockito.any());

        long start = faker.number().randomDigit();
        List<Long> ids = Arrays.asList(start++, start++, start++);
        Mockito.doReturn(ids).when(dataUploadChunkRepository).getChunkIds(Mockito.any());
        Mockito.doAnswer(invocation -> Optional.empty())
                .when(dataUploadChunkRepository).findById(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}/data", faker.number().randomDigit()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void getDataWithError() throws Exception {
        DataUpload dataUpload = new DataUpload();
        dataUpload.setSize(faker.number().randomNumber());
        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());
        Mockito.doReturn(dataUpload.getSize()).when(dataUploadChunkRepository).sumData(Mockito.any());

        long start = faker.number().randomDigit();
        List<Long> ids = Arrays.asList(start++, start++, start++);
        Mockito.doReturn(ids).when(dataUploadChunkRepository).getChunkIds(Mockito.any());
        DataUploadChunk uploadChunk = Mockito.mock(DataUploadChunk.class);
        Mockito.doAnswer(invocation -> {
            throw new IOException();
        }).when(uploadChunk).getData();
        Mockito.doAnswer(invocation -> Optional.of(uploadChunk))
                .when(dataUploadChunkRepository).findById(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}/data", faker.number().randomDigit()))
                .andExpect(status().isInternalServerError());
    }

    public List<DataUploadChunk> getChunks(int size) {
        List<DataUploadChunk> uploadChunks = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            DataUploadChunk dataUploadChunk = new DataUploadChunk();
            dataUploadChunk.setStart(1L);
            dataUploadChunk.setData(faker.backToTheFuture().quote().getBytes());
            uploadChunks.add(dataUploadChunk);
        }
        return uploadChunks;
    }
}