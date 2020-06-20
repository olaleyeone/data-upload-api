package com.github.olaleyeone.dataupload.integration.auth;

import com.github.olaleyeone.dataupload.integration.auth.SignatureKeyControllerApiFactory;
import com.github.olaleyeone.dataupload.test.component.ComponentTest;
import com.google.gson.Gson;
import com.olaleyeone.auth.api.SignatureKeyControllerApi;
import com.olaleyeone.auth.model.JsonWebKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Call;

import static org.junit.jupiter.api.Assertions.*;

class SignatureKeyControllerApiFactoryTest extends ComponentTest {

    private Gson gson;

    private SignatureKeyControllerApiFactory apiFactory;

    @BeforeEach
    void setUp() {
        gson = new Gson();
        apiFactory = new SignatureKeyControllerApiFactory("http://domain.com/", gson);
    }

    @Test
    void testInitWithIncompleteUrl() {
        apiFactory = new SignatureKeyControllerApiFactory("http://domain.com", gson);
        assertEquals("http://domain.com/", apiFactory.getBaseUrl());
    }

    @Test
    void testInitWithCompleteUrl() {
        apiFactory = new SignatureKeyControllerApiFactory("http://domain.com/", gson);
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
        SignatureKeyControllerApi apiClient = apiFactory.getObject();
        Call<JsonWebKey> call = apiClient.getJsonWebKey(faker.idNumber().valid());
        assertEquals("domain.com", call.request().url().host());
        assertFalse(call.request().url().isHttps());
    }
}