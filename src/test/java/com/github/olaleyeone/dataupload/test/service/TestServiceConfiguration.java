package com.github.olaleyeone.dataupload.test.service;

import com.github.olaleyeone.dataupload.data.dto.RequestMetadata;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestServiceConfiguration {

    @Bean
    public RequestMetadata requestMetadata() {
        return Mockito.mock(RequestMetadata.class);
    }
}
