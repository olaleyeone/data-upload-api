package com.github.olaleyeone.dataupload.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EntityScan({
        "com.olaleyeone.mailinglist.data.entity",
        "com.olaleyeone.audittrail.entity"
})
@EnableJpaRepositories({
        "com.olaleyeone.mailinglist.repository",
        "com.olaleyeone.audittrail.repository"
})
@EnableAsync
public class AppConfiguration {

}
