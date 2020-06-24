package com.github.olaleyeone.dataupload.bootstrap;

import com.github.olaleyeone.auth.configuration.AuthenticationConfiguration;
import com.github.olaleyeone.dataupload.configuration.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@Import({
        AppConfiguration.class,
        AuditTrailConfiguration.class,
        IntegrationConfiguration.class,
        WebConfiguration.class,
        AuthenticationConfiguration.class
})
@EnableAsync
public class DataUploadApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataUploadApplication.class, args);
    }

}
