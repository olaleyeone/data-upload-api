package com.github.olaleyeone.dataupload.messaging.listener;

import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.data.entity.DataUploadChunk;
import com.github.olaleyeone.dataupload.messaging.producer.CompletedUploadPublisher;
import com.github.olaleyeone.dataupload.repository.DataUploadRepository;
import com.github.olaleyeone.dataupload.test.entity.EntityTest;
import com.github.olaleyeone.entitysearch.JpaQuerySource;
import com.olaleyeone.audittrail.context.Action;
import com.olaleyeone.audittrail.impl.TaskContextFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CompletedUploadPublisherJobTest extends EntityTest {

    @Mock
    private CompletedUploadPublisher messageProducer;
    @Mock
    private TaskContextFactory taskContextFactory;
    @Autowired
    private JpaQuerySource jpaQuerySource;
    @Autowired
    private DataUploadRepository dataUploadRepository;

    private CompletedUploadPublisherJob completedUploadPublisherJob;

    @BeforeEach
    void setUp() {
        completedUploadPublisherJob = CompletedUploadPublisherJob.builder()
                .dataUploadRepository(dataUploadRepository)
                .jpaQuerySource(jpaQuerySource)
                .messageProducer(messageProducer)
                .taskContextFactory(taskContextFactory)
                .build();

        Mockito.doAnswer(invocation -> {
            ((Action) invocation.getArgument(2)).execute();
            return null;
        }).when(taskContextFactory).startBackgroundTask(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void listenWithData() {
        int size = 100;
        DataUploadChunk dataUploadChunk = modelFactory.pipe(DataUploadChunk.class)
                .then(it -> {
                    it.setData(new byte[size]);
                    it.setCreatedOn(LocalDateTime.now().minusMinutes(1));
                    DataUpload dataUpload = it.getDataUpload();
                    dataUpload.setSize(Long.valueOf(size));
                    dataUpload.setCompletionPublishedOn(null);
                    return it;
                })
                .create();
        DataUpload dataUpload = dataUploadChunk.getDataUpload();

        Mockito.doAnswer(invocation -> {
            dataUpload.setCompletionPublishedOn(LocalDateTime.now());
            dataUploadRepository.save(dataUpload);
            return CompletableFuture.completedFuture(null);
        }).when(messageProducer).send(Mockito.any());
        completedUploadPublisherJob.listen("");
        assertNotNull(dataUpload.getCompletionPublishedOn());
    }

    @Test
    void listenWithoutData() {
        int size = 100;
        DataUploadChunk dataUploadChunk = modelFactory.pipe(DataUploadChunk.class)
                .then(it -> {
                    it.setData(new byte[size / 2]);
                    it.setCreatedOn(LocalDateTime.now().minusMinutes(1));
                    DataUpload dataUpload = it.getDataUpload();
                    dataUpload.setSize(Long.valueOf(size));
                    dataUpload.setCompletionPublishedOn(null);
                    return it;
                })
                .create();
        DataUpload dataUpload = dataUploadChunk.getDataUpload();
        completedUploadPublisherJob.listen("");
        Mockito.verify(messageProducer, Mockito.never()).send(Mockito.any());
        assertNull(dataUpload.getCompletionPublishedOn());
    }

    @Test
    void listenWithException() {
        int size = 100;
        modelFactory.pipe(DataUploadChunk.class)
                .then(it -> {
                    it.setData(new byte[size]);
                    it.setCreatedOn(LocalDateTime.now().minusMinutes(1));
                    DataUpload dataUpload = it.getDataUpload();
                    dataUpload.setSize(Long.valueOf(size));
                    dataUpload.setCompletionPublishedOn(null);
                    return it;
                })
                .create();

        Mockito.doAnswer(invocation -> {
            throw new RuntimeException();
        }).when(messageProducer).send(Mockito.any());
        completedUploadPublisherJob.listen("");
        Mockito.verify(messageProducer, Mockito.times(1)).send(Mockito.any());
    }
}