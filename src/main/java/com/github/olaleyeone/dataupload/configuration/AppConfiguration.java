package com.github.olaleyeone.dataupload.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({DataConfiguration.class})
@ComponentScan({"com.github.olaleyeone.dataupload.service"})
public class AppConfiguration {
}
