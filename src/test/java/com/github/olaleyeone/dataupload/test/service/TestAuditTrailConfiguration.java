package com.github.olaleyeone.dataupload.test.service;

import com.olaleyeone.audittrail.api.EntityDataExtractor;
import com.olaleyeone.audittrail.configuration.AuditTrailConfiguration;
import org.mockito.Mockito;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

@Configuration
public class TestAuditTrailConfiguration extends AuditTrailConfiguration {

    @Override
    public EntityDataExtractor entityDataExtractor(EntityManager entityManager) {
        return Mockito.mock(EntityDataExtractor.class);
    }
}
