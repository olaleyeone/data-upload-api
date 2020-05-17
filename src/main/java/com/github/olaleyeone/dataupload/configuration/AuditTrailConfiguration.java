package com.github.olaleyeone.dataupload.configuration;

import com.olaleyeone.audittrail.api.EntityDataExtractor;
import com.olaleyeone.audittrail.impl.EntityDataExtractorImpl;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

@Configuration
public class AuditTrailConfiguration extends com.olaleyeone.audittrail.configuration.AuditTrailConfiguration {

    @Override
    @Bean
    public EntityDataExtractor entityDataExtractor(EntityManager entityManager) {
        return new EntityDataExtractorImpl(entityManager) {
            @Override
            public Class<?> getType(Object e) {
                if (e instanceof HibernateProxy) {
                    return ((HibernateProxy) e).getHibernateLazyInitializer().getPersistentClass();
                }
                return e.getClass();
            }
        };
    }
}
