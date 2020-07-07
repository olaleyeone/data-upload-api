package com.github.olaleyeone.dataupload.test.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.heywhy.springentityfactory.contracts.ModelFactory;
import com.github.javafaker.Faker;
import com.github.olaleyeone.configuration.BeanValidationConfiguration;
import com.github.olaleyeone.dataupload.test.dto.DtoFactory;
import com.github.olaleyeone.dataupload.test.entity.EntityFactoryConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.internal.creation.bytebuddy.MockAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Random;

@ActiveProfiles("test")
@WebMvcTest
@ContextConfiguration(classes = {
        ControllerTestConfiguration.class,
        BeanValidationConfiguration.class
})
public class ControllerTest {

    protected final Faker faker = Faker.instance(new Random());
    protected final DtoFactory dtoFactory = new DtoFactory(faker);
    protected final ModelFactory modelFactory = new EntityFactoryConfiguration().entityFactory(faker, null);

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private ApplicationContext applicationContext;

    protected RequestPostProcessor body(Object body) {
        return request -> {
            request.setContentType(MediaType.APPLICATION_JSON_VALUE);
            try {
                request.setContent(objectMapper.writeValueAsBytes(body));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return request;
        };
    }

    @BeforeEach
    public void resetMocks() {
        applicationContext.getBeansOfType(MockAccess.class)
                .values().forEach(Mockito::reset);
    }
}
