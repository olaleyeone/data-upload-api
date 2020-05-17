package com.github.olaleyeone.dataupload.test.service;

import com.github.olaleyeone.dataupload.test.entity.EntityTest;
import com.olaleyeone.audittrail.embeddable.Duration;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.impl.TaskContextHolder;
import com.olaleyeone.audittrail.impl.TaskContextImpl;
import com.olaleyeone.audittrail.impl.TaskTransactionContextFactory;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.internal.creation.bytebuddy.MockAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

@Import({ServiceTestConfiguration.class, TestAuditTrailConfiguration.class})
public class ServiceTest extends EntityTest {

    @Autowired
    private TaskContextHolder taskContextHolder;

    @Autowired
    private TaskTransactionContextFactory taskTransactionContextFactory;

    @BeforeEach
    public void resetMocks() {
        applicationContext.getBeansOfType(MockAccess.class)
                .values().forEach(Mockito::reset);

        Task task = new Task();
        task.setDuration(new Duration(LocalDateTime.now(), null));
        task.setName(faker.funnyName().name());
        task.setType(faker.app().name());
        taskContextHolder.registerContext(new TaskContextImpl(task, null, taskContextHolder, taskTransactionContextFactory));
    }
}
