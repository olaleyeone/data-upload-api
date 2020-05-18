package com.github.olaleyeone.dataupload.event.listeners;

import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.event.mesage.UploadCompleteEvent;
import com.github.olaleyeone.dataupload.repository.DataUploadRepository;
import com.olaleyeone.audittrail.context.TaskContext;
import com.olaleyeone.audittrail.impl.TaskContextFactory;
import lombok.RequiredArgsConstructor;
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
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class UploadCompleteEventListener {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final TransactionTemplate transactionTemplate;
    private final DataUploadRepository dataUploadRepository;

    private final Provider<TaskContext> taskContextProvider;
    private final TaskContextFactory taskContextFactory;

    @Value("${completed_upload.topic.name}")
    private String completedUploadTopic;

    @EventListener(UploadCompleteEvent.class)
    @Async
    public void newUserCreated(UploadCompleteEvent newUserEvent) {
        logger.info("Event: Upload completed");
        DataUpload portalUser = newUserEvent.getDataUpload();
        taskContextFactory.startBackgroundTask(
                "PUBLISH COMPLETED UPLOAD",
                String.format("Publish completed upload %d", portalUser.getId()),
                () -> sendUser(portalUser));
    }

    private void sendUser(DataUpload dataUpload) {
        sendMessage(dataUpload).addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

            @Override
            public void onFailure(Throwable ex) {
                //noop
                logger.error(ex.getMessage(), ex);
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
                logger.info("Upload published");
                taskContextProvider.get().execute(
                        "UPDATE PUBLISHED UPLOAD",
                        String.format("Update published upload %d", dataUpload.getId()),
                        () -> transactionTemplate.execute(status -> {
                            dataUpload.setCompletionPublishedOn(LocalDateTime.now());
                            dataUploadRepository.save(dataUpload);
                            return null;
                        }));
            }
        });
    }

    public ListenableFuture<SendResult<String, String>> sendMessage(DataUpload msg) {
        return kafkaTemplate.send(completedUploadTopic, msg.getId().toString());
    }
}
