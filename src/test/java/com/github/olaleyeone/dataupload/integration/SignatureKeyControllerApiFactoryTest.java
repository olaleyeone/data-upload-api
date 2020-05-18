package com.github.olaleyeone.dataupload.integration;

import com.github.olaleyeone.dataupload.test.component.ComponentTest;
import com.olaleyeone.auth.api.SignatureKeyControllerApi;
import com.olaleyeone.auth.model.JsonWebKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Call;

import static org.junit.jupiter.api.Assertions.*;

class SignatureKeyControllerApiFactoryTest extends ComponentTest {

    private SignatureKeyControllerApiFactory apiFactory;

    @BeforeEach
    void setUp() {
        apiFactory = new SignatureKeyControllerApiFactory();
        apiFactory.setBaseUrl("http://domain.com/");
    }

    @Test
    void testInitWithIncompleteUrl() {
        apiFactory.setBaseUrl("http://domain.com");
        apiFactory.init();
        assertEquals("http://domain.com/", apiFactory.getBaseUrl());
    }

    @Test
    void testInitWithCompleteUrl() {
        apiFactory.setBaseUrl("http://domain.com/");
        apiFactory.init();
        assertEquals("http://domain.com/", apiFactory.getBaseUrl());
    }

    @Test
    void getObjectType() {
        assertEquals(SignatureKeyControllerApi.class, apiFactory.getObjectType());
    }

    @Test
    void isSingleton() {
        assertTrue(apiFactory.isSingleton());
    }

    @Test
    void getObject() {
        apiFactory.setBaseUrl("http://domain.com/");
        SignatureKeyControllerApi apiClient = apiFactory.getObject();
        String body = faker.backToTheFuture().quote();
        Call<JsonWebKey> call = apiClient.getJsonWebKey(faker.idNumber().valid());
        assertEquals("domain.com", call.request().url().host());
        assertFalse(call.request().url().isHttps());
    }
}