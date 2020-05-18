package com.github.olaleyeone.dataupload.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.olaleyeone.dataupload.configuration.BeanValidationConfiguration;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.repository.DataUploadChunkRepository;
import com.github.olaleyeone.dataupload.response.pojo.DataUploadApiResponse;
import com.github.olaleyeone.dataupload.test.service.ServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({
        UploadTest.$Config.class
})
@ComponentScan({
        "com.github.olaleyeone.dataupload.controller",
        "com.github.olaleyeone.dataupload.advice",
        "com.github.olaleyeone.dataupload.response.handler"
})
@AutoConfigureWebMvc
@AutoConfigureMockMvc
public class UploadTest extends ServiceTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private DataUploadChunkRepository dataUploadChunkRepository;

    @Test
    public void testUpload() throws Exception {
        byte[] initialData = faker.lorem().paragraphs(20).stream()
                .reduce((a, b) -> a + "\n\n" + b).get().getBytes();

        DataUpload dataUpload = modelFactory.create(DataUpload.class);

        try (InputStream inputStream = new ByteArrayInputStream(initialData)) {
            byte[] dataRead = new byte[256];
            AtomicLong totalRead = new AtomicLong(0);
            int read;
            while ((read = inputStream.read(dataRead, 0, dataRead.length)) > 0) {
                byte[] dataSent = Arrays.copyOfRange(dataRead, 0, read);
                mockMvc.perform(MockMvcRequestBuilders.post("/uploads/{id}/data/{start}", dataUpload.getId(), totalRead.get() + 1)
                        .param("totalSize", String.valueOf(initialData.length))
                        .contentType("text/plain")
                        .content(dataSent))
                        .andExpect(status().isOk())
                        .andExpect(result -> {
                            DataUploadApiResponse apiResponse = objectMapper.readValue(result.getResponse().getContentAsByteArray(), DataUploadApiResponse.class);
                            assertNotNull(apiResponse);
                            assertEquals(totalRead.get() + dataSent.length, apiResponse.getSizeUploaded());
                        });
                totalRead.addAndGet(read);
            }
        }

        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}/data", dataUpload.getId()))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    assertArrayEquals(initialData, result.getResponse().getContentAsByteArray());
                });
    }

    @Configuration
    @Import({
            BeanValidationConfiguration.class
    })
    static class $Config implements WebMvcConfigurer {

    }
}
