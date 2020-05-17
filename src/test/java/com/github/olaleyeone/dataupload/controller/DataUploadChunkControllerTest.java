package com.github.olaleyeone.dataupload.controller;

import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.repository.DataUploadChunkRepository;
import com.github.olaleyeone.dataupload.repository.DataUploadRepository;
import com.github.olaleyeone.dataupload.response.handler.DataUploadApiResponseHandler;
import com.github.olaleyeone.dataupload.response.pojo.DataUploadApiResponse;
import com.github.olaleyeone.dataupload.service.api.DataUploadChunkService;
import com.github.olaleyeone.dataupload.test.controller.ControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DataUploadChunkControllerTest extends ControllerTest {

    @Autowired
    private DataUploadRepository dataUploadRepository;

    @Autowired
    private DataUploadChunkRepository dataUploadChunkRepository;

    @Autowired
    private DataUploadChunkService dataUploadChunkService;

    @Autowired
    private DataUploadApiResponseHandler dataUploadApiResponseHandler;

    private DataUpload dataUpload;
    private DataUploadApiResponse dataUploadApiResponse;

    @BeforeEach
    public void setUp() {
        dataUpload = modelFactory.make(DataUpload.class);
        dataUpload.setId(faker.number().randomNumber());
        dataUploadApiResponse = new DataUploadApiResponse(dataUpload);
        Mockito.doReturn(dataUploadApiResponse).when(dataUploadApiResponseHandler).getDataUploadApiResponse(Mockito.any());
    }

    @Test
    void uploadChunkWithInvalidStart() throws Exception {

        String data = faker.beer().malt();
        dataUpload.setSize(Long.valueOf(data.getBytes().length / 2));
        mockMvc.perform(MockMvcRequestBuilders.post("/uploads/{id}/chunks/{start}", dataUpload.getId(), 0)
                .with(body(data)))
                .andExpect(status().isBadRequest());
        Mockito.verify(dataUploadChunkService, Mockito.never()).createChunk(Mockito.eq(dataUpload), Mockito.any());
    }

    @Test
    void uploadChunkWithExcessData() throws Exception {

        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());

        String data = faker.beer().malt();
        dataUpload.setSize(Long.valueOf(data.getBytes().length / 2));
        mockMvc.perform(MockMvcRequestBuilders.post("/uploads/{id}/chunks/{start}", dataUpload.getId(), 1)
                .with(body(data)))
                .andExpect(status().isBadRequest());
        Mockito.verify(dataUploadChunkService, Mockito.never()).createChunk(Mockito.eq(dataUpload), Mockito.any());
    }

    @Test
    void uploadInFull() throws Exception {

        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());

        String data = faker.beer().malt();
        dataUpload.setSize(Long.valueOf(data.getBytes().length));
        mockMvc.perform(MockMvcRequestBuilders.post("/uploads/{id}/chunks/{start}", dataUpload.getId(), 1)
                .content(data))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    DataUploadApiResponse apiResponse = objectMapper.readValue(result.getResponse().getContentAsByteArray(), DataUploadApiResponse.class);
                    assertEquals(this.dataUploadApiResponse, apiResponse);
                });
        Mockito.verify(dataUploadApiResponseHandler, Mockito.times(1)).getDataUploadApiResponse(dataUpload);
        Mockito.verify(dataUploadChunkService, Mockito.times(1)).createChunk(Mockito.eq(dataUpload), Mockito.any());
    }

    @Test
    void uploadChunkWithInconsistentContentType() throws Exception {

        dataUpload.setContentType("text/plain");
        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());

        String data = faker.beer().malt();
        dataUpload.setSize(Long.valueOf(data.getBytes().length));
        mockMvc.perform(MockMvcRequestBuilders.post("/uploads/{id}/chunks/{start}", dataUpload.getId(), 1)
                .content(data))
                .andExpect(status().isBadRequest());
        Mockito.verify(dataUploadChunkService, Mockito.never()).createChunk(Mockito.eq(dataUpload), Mockito.any());
    }

    @Test
    void uploadChunkWithConsistentContentType() throws Exception {

        dataUpload.setContentType("text/plain");
        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());

        String data = faker.beer().malt();
        dataUpload.setSize(Long.valueOf(data.getBytes().length));
        mockMvc.perform(MockMvcRequestBuilders.post("/uploads/{id}/chunks/{start}", dataUpload.getId(), 1)
                .contentType(dataUpload.getContentType())
                .content(data))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    DataUploadApiResponse apiResponse = objectMapper.readValue(result.getResponse().getContentAsByteArray(), DataUploadApiResponse.class);
                    assertEquals(this.dataUploadApiResponse, apiResponse);
                });
        Mockito.verify(dataUploadApiResponseHandler, Mockito.times(1)).getDataUploadApiResponse(dataUpload);
        Mockito.verify(dataUploadChunkService, Mockito.times(1)).createChunk(Mockito.eq(dataUpload), Mockito.any());
    }

    @Test
    void uploadChunkWithConflict() throws Exception {

        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());

        Mockito.doReturn(1).when(dataUploadChunkRepository).countByRange(Mockito.any(), Mockito.any(), Mockito.any());

        String data = faker.beer().malt();
        dataUpload.setSize(Long.valueOf(data.getBytes().length));
        mockMvc.perform(MockMvcRequestBuilders.post("/uploads/{id}/chunks/{start}", dataUpload.getId(), 1)
                .content(data))
                .andExpect(status().isBadRequest());
        Mockito.verify(dataUploadChunkService, Mockito.never()).createChunk(Mockito.eq(dataUpload), Mockito.any());
    }

    @Test
    void uploadChunkWithConflictAfterSaving() throws Exception {

        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());

        Mockito.doReturn(0, 2).when(dataUploadChunkRepository).countByRange(Mockito.any(), Mockito.any(), Mockito.any());

        String data = faker.beer().malt();
        dataUpload.setSize(Long.valueOf(data.getBytes().length));
        mockMvc.perform(MockMvcRequestBuilders.post("/uploads/{id}/chunks/{start}", dataUpload.getId(), 1)
                .content(data))
                .andExpect(status().isBadRequest());

        Mockito.verify(dataUploadChunkService, Mockito.times(1)).createChunk(Mockito.eq(dataUpload), Mockito.any());
        Mockito.verify(dataUploadChunkService, Mockito.times(1)).delete(Mockito.any());
    }
}