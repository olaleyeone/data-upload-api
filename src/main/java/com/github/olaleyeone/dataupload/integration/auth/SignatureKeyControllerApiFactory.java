package com.github.olaleyeone.dataupload.integration.auth;

import com.google.gson.Gson;
import com.olaleyeone.auth.api.SignatureKeyControllerApi;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@RequiredArgsConstructor
@Component
public class SignatureKeyControllerApiFactory implements FactoryBean<SignatureKeyControllerApi> {

    @Value("${auth.api.base_url}")
    private final String baseUrl;

    private final Gson gson;

    @Override
    public Class<?> getObjectType() {
        return SignatureKeyControllerApi.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public SignatureKeyControllerApi getObject() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(getOkHttpClient())
                .validateEagerly(true)
                .build();
        return retrofit.create(SignatureKeyControllerApi.class);
    }

    private OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .build();
    }

    public String getBaseUrl() {
        if (!baseUrl.endsWith("/")) {
            return baseUrl + "/";
        }
        return baseUrl;
    }
}
