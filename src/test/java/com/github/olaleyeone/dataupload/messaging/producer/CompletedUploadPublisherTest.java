package com.github.olaleyeone.dataupload.messaging.producer;

import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.messaging.event.UploadCompletedEvent;
import com.github.olaleyeone.dataupload.repository.DataUploadChunkRepository;
import com.github.olaleyeone.dataupload.repository.DataUploadRepository;
import com.github.olaleyeone.dataupload.response.pojo.DataUploadApiResponse;
import com.github.olaleyeone.dataupload.test.component.ComponentTest;
import com.olaleyeone.audittrail.context.Action;
import com.olaleyeone.audittrail.context.TaskContext;
import com.olaleyeone.audittrail.impl.TaskContextFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Provider;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CompletedUploadPublisherTest extends ComponentTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    private TransactionTemplate transactionTemplate;
    @Mock
    private DataUploadRepository dataUploadRepository;
    @Mock
    private TaskContext taskContext;
    @Mock
    private Provider<TaskContext> taskContextProvider;
    @Mock
    private TaskContextFactory taskContextFactory;
    @Mock
    private DataUploadChunkRepository dataUploadChunkRepository;

    @InjectMocks
    private CompletedUploadPublisher completedUploadPublisher;

    private DataUpload dataUpload;

    @BeforeEach
    public void setUp() {
        dataUpload = modelFactory.make(DataUpload.class);
        dataUpload.setId(faker.number().randomNumber());
    }

    @Test
    void uploadCompletedEvent() {
        Answer answer = invocation -> {
            invocation.getArgument(2, Action.class).execute();
            return null;
        };
        Mockito.doAnswer(answer).when(taskContextFactory).startBackgroundTask(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doAnswer(answer).when(taskContext).execute(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doReturn(taskContext).when(taskContextProvider).get();
        Mockito.doAnswer(invocation -> invocation.getArgument(0, TransactionCallback.class).doInTransaction(null))
                .when(transactionTemplate).execute(Mockito.any());
        Mockito.doReturn(new AsyncResult<>(Mockito.mock(SendResult.class)))
                .when(kafkaTemplate)
                .send(Mockito.any(), Mockito.any(), Mockito.any());
        OffsetDateTime now = OffsetDateTime.now().minusSeconds(faker.number().randomDigit());
        Mockito.doReturn(now).when(dataUploadChunkRepository).findLatestUploadTime(Mockito.any());

        completedUploadPublisher.uploadCompletedEvent(new UploadCompletedEvent(dataUpload));
        assertEquals(now, dataUpload.getCompletedOn());
        assertNotNull(dataUpload.getCompletionPublishedOn());
        Mockito.verify(dataUploadRepository, Mockito.times(1))
                .save(dataUpload);
    }

    @Test
    void uploadCompletedEventWithError() {
        Mockito.doAnswer(invocation -> {
            invocation.getArgument(2, Action.class).execute();
            return null;
        }).when(taskContextFactory).startBackgroundTask(Mockito.any(), Mockito.any(), Mockito.any());

        Mockito.doReturn(AsyncResult.forExecutionException(new RuntimeException()))
                .when(kafkaTemplate)
                .send(Mockito.any(), Mockito.any(), Mockito.any());

        completedUploadPublisher.uploadCompletedEvent(new UploadCompletedEvent(dataUpload));
        assertNull(dataUpload.getCompletionPublishedOn());
        Mockito.verify(dataUploadRepository, Mockito.never())
                .save(dataUpload);
    }

    @Test
    void sendMessage() {
        completedUploadPublisher.setCompletedUploadTopic(faker.address().city());
        completedUploadPublisher.sendMessage(dataUpload);
        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(
                        completedUploadPublisher.getCompletedUploadTopic(),
                        dataUpload.getId().toString(),
                        new DataUploadApiResponse(dataUpload));
    }
}