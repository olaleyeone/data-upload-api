package com.github.olaleyeone.dataupload.configuration;

import com.github.olaleyeone.auth.factory.SignatureKeyControllerApiFactory;
import com.github.olaleyeone.converter.LocalDateTimeTypeAdapter;
import com.github.olaleyeone.converter.LocalDateTypeAdapter;
import com.github.olaleyeone.converter.OffsetDateTimeTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.github.olaleyeone.configuration.JacksonConfiguration.DEFAULT_DATE_TIME_FORMAT;

@Configuration
@ComponentScan({
        "com.github.olaleyeone.dataupload.integration",
        "com.github.olaleyeone.dataupload.messaging"
})
@Import({
        KafkaProducerConfig.class
})
public class IntegrationConfiguration {

    @Value("${auth.api.base_url}")
    private String baseUrl;

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .setDateFormat(DEFAULT_DATE_TIME_FORMAT)
                .registerTypeAdapterFactory(OffsetDateTimeTypeAdapter.FACTORY)
                .registerTypeAdapterFactory(LocalDateTimeTypeAdapter.FACTORY)
                .registerTypeAdapterFactory(LocalDateTypeAdapter.FACTORY)
                .create();
    }

    public String getBaseUrl() {
        if (!baseUrl.endsWith("/")) {
            return baseUrl + "/";
        }
        return baseUrl;
    }

    @Bean
    public SignatureKeyControllerApiFactory signatureKeyControllerApiFactory(Gson gson) {
        return new SignatureKeyControllerApiFactory(getBaseUrl(), gson);
    }
}
