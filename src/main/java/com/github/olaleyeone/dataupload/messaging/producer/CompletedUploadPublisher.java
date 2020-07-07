package com.github.olaleyeone.dataupload.messaging.producer;

import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.messaging.event.UploadCompletedEvent;
import com.github.olaleyeone.dataupload.repository.DataUploadRepository;
import com.github.olaleyeone.dataupload.response.pojo.DataUploadApiResponse;
import com.olaleyeone.audittrail.context.TaskContext;
import com.olaleyeone.audittrail.impl.TaskContextFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.inject.Provider;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@RequiredArgsConstructor
@Component
public class CompletedUploadPublisher {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final TransactionTemplate transactionTemplate;
    private final DataUploadRepository dataUploadRepository;

    private final Provider<TaskContext> taskContextProvider;
    private final TaskContextFactory taskContextFactory;

    @Value("${completed_upload.topic.name}")
    @Getter
    @Setter
    private String completedUploadTopic;

    @EventListener(UploadCompletedEvent.class)
    @Async
    public void uploadCompletedEvent(UploadCompletedEvent event) {
        logger.info("Event: Upload of {} completed", event.getDataUpload().getId());
        DataUpload dataUpload = event.getDataUpload();
        taskContextFactory.startBackgroundTask(
                "PUBLISH COMPLETED UPLOAD",
                String.format("Publish completed upload %d", dataUpload.getId()),
                () -> send(dataUpload));
    }

    public Future<?> send(DataUpload dataUpload) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        sendMessage(dataUpload).addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {

            @Override
            public void onFailure(Throwable ex) {
                logger.error(ex.getMessage(), ex);
                completableFuture.completeExceptionally(ex);
            }

            @Override
            public void onSuccess(SendResult<String, Object> result) {
                logger.info("Upload of {} published", dataUpload.getId());
                try {
                    taskContextFactory.startBackgroundTask(
                            "UPDATE PUBLISHED UPLOAD",
                            String.format("Update published upload %d", dataUpload.getId()),
                            () -> taskContextProvider.get().execute(
                                    "UPDATE PUBLISHED UPLOAD",
                                    String.format("Update published upload %d", dataUpload.getId()),
                                    () -> transactionTemplate.execute(status -> {
                                        dataUpload.setCompletionPublishedOn(OffsetDateTime.now());
                                        return dataUploadRepository.save(dataUpload);
                                    })));
                } finally {
                    completableFuture.complete(null);
                }
            }
        });
        return completableFuture;
    }

    protected ListenableFuture<SendResult<String, Object>> sendMessage(DataUpload msg) {
        return kafkaTemplate.send(completedUploadTopic, msg.getId().toString(), new DataUploadApiResponse(msg));
    }
}
