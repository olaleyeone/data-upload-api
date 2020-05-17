package com.github.olaleyeone.dataupload.test.controller;

import com.github.olaleyeone.dataupload.repository.DataUploadChunkRepository;
import com.github.olaleyeone.dataupload.repository.DataUploadRepository;
import com.github.olaleyeone.dataupload.response.handler.DataUploadApiResponseHandler;
import com.github.olaleyeone.dataupload.service.api.DataUploadChunkService;
import com.github.olaleyeone.dataupload.service.api.DataUploadService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        "com.github.olaleyeone.dataupload.controller",
        "com.github.olaleyeone.dataupload.advice",
})
public class ControllerTestConfiguration {

    @Bean
    public DataUploadService dataUploadService() {
        return Mockito.mock(DataUploadService.class);
    }

    @Bean
    public DataUploadChunkService dataUploadChunkService() {
        return Mockito.mock(DataUploadChunkService.class);
    }

    @Bean
    public DataUploadRepository dataUploadRepository() {
        return Mockito.mock(DataUploadRepository.class);
    }

    @Bean
    public DataUploadChunkRepository dataUploadChunkRepository() {
        return Mockito.mock(DataUploadChunkRepository.class);
    }

    @Bean
    public DataUploadApiResponseHandler dataUploadApiResponseHandler() {
        return Mockito.mock(DataUploadApiResponseHandler.class);
    }
}
