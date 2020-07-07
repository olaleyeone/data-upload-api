package com.github.olaleyeone.dataupload.messaging.listener;

import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.data.entity.DataUploadChunk;
import com.github.olaleyeone.dataupload.data.entity.QDataUpload;
import com.github.olaleyeone.dataupload.data.entity.QDataUploadChunk;
import com.github.olaleyeone.dataupload.messaging.producer.CompletedUploadPublisher;
import com.github.olaleyeone.dataupload.repository.DataUploadRepository;
import com.github.olaleyeone.entitysearch.JpaQuerySource;
import com.olaleyeone.audittrail.impl.TaskContextFactory;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Builder
@Component
public class CompletedUploadPublisherJob {

    final Logger logger = LoggerFactory.getLogger(getClass());

    private final JpaQuerySource jpaQuerySource;
    private final CompletedUploadPublisher messageProducer;
    private final TaskContextFactory taskContextFactory;
    private final DataUploadRepository dataUploadRepository;

    private final AtomicReference<LocalDateTime> lastTrigger = new AtomicReference<>();
    private final Lock lock = new ReentrantLock();

    @SneakyThrows
    @KafkaListener(topics = "${task.publish_completed_uploads.topic.name}", groupId = "${kafka.groupId}")
    public void listen(String message) {
        logger.info("{}", message);
        LocalDateTime startTime = LocalDateTime.now();
        lastTrigger.set(startTime);
        taskContextFactory.startBackgroundTask(
                "PUBLISH COMPLETED UPLOADS",
                "Start background job to publish completed uploads",
                () -> {
                    try {
                        lock.lock();
                        processQueue(startTime);
                    } finally {
                        lock.unlock();
                    }
                    logger.info("session ended");
                });
    }

    private void processQueue(LocalDateTime startTime) {
        List<DataUpload> failures = new ArrayList<>();
        List<DataUpload> dataUploads;
        do {
            dataUploads = getNext(failures.size());
            dataUploads.forEach(dataUpload -> {
                try {
                    messageProducer.send(dataUpload).get();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    failures.add(dataUpload);
                }
            });
            if (startTime != lastTrigger.get()) {
                break;
            }
        } while (!dataUploads.isEmpty());
    }

    private List<DataUpload> getNext(int offset) {
        JPAQuery<DataUploadChunk> jpaQuery = jpaQuerySource.startQuery(QDataUploadChunk.dataUploadChunk);
        jpaQuery.innerJoin(QDataUpload.dataUpload)
                .on(QDataUploadChunk.dataUploadChunk.dataUpload.eq(QDataUpload.dataUpload));
        List<Tuple> tuples = jpaQuery
                .where(QDataUpload.dataUpload.completionPublishedOn.isNull())
                .select(QDataUpload.dataUpload.id,
                        QDataUpload.dataUpload.size,
                        QDataUploadChunk.dataUploadChunk.size.sum().longValue(),
                        QDataUploadChunk.dataUploadChunk.createdOn.max())
                .groupBy(QDataUpload.dataUpload.id, QDataUpload.dataUpload.size)
                .having(QDataUpload.dataUpload.size.eq(QDataUploadChunk.dataUploadChunk.size.sum().longValue()))
                .having(QDataUploadChunk.dataUploadChunk.createdOn.max().before(OffsetDateTime.now()
                        .minus(5, ChronoUnit.SECONDS)))
                .orderBy(QDataUploadChunk.dataUploadChunk.createdOn.max().asc())
                .offset(offset)
                .limit(20)
                .fetch();
        List<Long> ids = tuples.stream()
                .map(tuple -> tuple.get(0, Long.class))
                .collect(Collectors.toList());
        return ids.isEmpty() ? Collections.EMPTY_LIST : dataUploadRepository.findAllById(ids);
    }
}
