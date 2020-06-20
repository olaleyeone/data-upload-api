package com.github.olaleyeone.dataupload.bootstrap;

import com.github.olaleyeone.dataupload.messaging.listener.CompletedUploadPublisherJob;
import com.github.olaleyeone.dataupload.messaging.producer.CompletedUploadPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class DataUploadApplicationTests {

    @Autowired
    public ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        applicationContext.getBean(CompletedUploadPublisher.class);
        applicationContext.getBean(CompletedUploadPublisherJob.class);
    }

}
