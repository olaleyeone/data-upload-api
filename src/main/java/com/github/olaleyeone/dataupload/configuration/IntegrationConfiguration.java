package com.github.olaleyeone.dataupload.configuration;

import com.github.olaleyeone.dataupload.converter.LocalDateTimeTypeAdapter;
import com.github.olaleyeone.dataupload.converter.LocalDateTypeAdapter;
import com.github.olaleyeone.dataupload.converter.OffsetDateTimeTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@ComponentScan({
        "com.github.olaleyeone.dataupload.integration"
})
@Import({
        KafkaTopicConfig.class,
        KafkaProducerConfig.class
})
@EnableAsync
public class IntegrationConfiguration {

    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .setDateFormat(DEFAULT_DATE_TIME_FORMAT)
                .registerTypeAdapterFactory(OffsetDateTimeTypeAdapter.FACTORY)
                .registerTypeAdapterFactory(LocalDateTimeTypeAdapter.FACTORY)
                .registerTypeAdapterFactory(LocalDateTypeAdapter.FACTORY)
                .create();
    }
}
