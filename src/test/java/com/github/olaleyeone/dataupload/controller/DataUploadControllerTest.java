package com.github.olaleyeone.dataupload.controller;

import com.github.olaleyeone.dataupload.data.dto.DataUploadApiRequest;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.response.pojo.DataUploadApiResponse;
import com.github.olaleyeone.dataupload.service.api.DataUploadService;
import com.github.olaleyeone.dataupload.test.controller.ControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DataUploadControllerTest extends ControllerTest {

    @Autowired
    private DataUploadService dataUploadService;

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
}