package com.github.olaleyeone.dataupload.bootstrap;

import com.github.olaleyeone.dataupload.configuration.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication

@Import({
        AppConfiguration.class,
        AuditTrailConfiguration.class,
        IntegrationConfiguration.class,
        WebConfiguration.class,
        SecurityConfiguration.class,
        OpenApiConfiguration.class
})
public class DataUploadApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataUploadApplication.class, args);
    }

}
