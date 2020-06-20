package com.github.olaleyeone.dataupload.interceptor;

import com.github.olaleyeone.auth.data.AccessClaims;
import com.github.olaleyeone.auth.data.AuthorizedRequest;
import com.github.olaleyeone.dataupload.test.component.ComponentTest;
import com.github.olaleyeone.interceptor.TaskContextHandlerInterceptor;
import com.olaleyeone.audittrail.embeddable.Duration;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.WebRequest;
import com.olaleyeone.audittrail.impl.TaskContextFactory;
import com.olaleyeone.audittrail.impl.TaskContextHolder;
import com.olaleyeone.audittrail.impl.TaskContextImpl;
import com.olaleyeone.audittrail.impl.TaskContextSaver;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaskContextHandlerInterceptorTest extends ComponentTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private TaskContextFactory taskContextFactory;

    @Mock
    private TaskContextHolder taskContextHolder;

    @Mock
    private TaskContextSaver taskContextSaver;

    @Mock
    private AuthorizedRequest authorizedRequest;

    @Mock
    private AccessClaims accessClaims;

    @Mock
    private Provider<AuthorizedRequest> authorizedRequestProvider;

    @InjectMocks
    private TaskContextHandlerInterceptor taskContextHandlerInterceptor;

    @Test
    void preHandle() {
        Mockito.doReturn(authorizedRequest).when(authorizedRequestProvider).get();
        String path = faker.internet().slug();
        Mockito.doReturn(path).when(request).getServletPath();
        Mockito.doReturn(path).when(request).getRequestURI();
        String ipV4Address = faker.internet().ipV4Address();
        Mockito.doReturn(ipV4Address).when(request).getRemoteAddr();
        String userAgent = faker.internet().userAgentAny();
        Mockito.doReturn(userAgent).when(request).getHeader(Mockito.eq(HttpHeaders.USER_AGENT));
        taskContextHandlerInterceptor.preHandle(request, response, null);
        Mockito.verify(taskContextFactory, Mockito.times(1))
                .start(Mockito.argThat(task -> {
                    assertNotNull(task);
                    assertNotNull(task.getDuration());
                    assertEquals(Task.WEB_REQUEST, task.getType());
                    assertEquals(path, task.getName());

                    assertNotNull(task.getWebRequest());
                    WebRequest webRequest = task.getWebRequest();
                    assertEquals(path, webRequest.getUri());
                    assertEquals(ipV4Address, webRequest.getIpAddress());
                    assertEquals(userAgent, webRequest.getUserAgent());
                    return true;
                }));
    }

    @Test
    void preHandleAuthorizedRequest() {
        Mockito.doReturn(authorizedRequest).when(authorizedRequestProvider).get();
        Mockito.doReturn(accessClaims).when(authorizedRequest).getAccessClaims();
        Mockito.doReturn(faker.number().digit()).when(accessClaims).getSubject();
        Mockito.doReturn(faker.number().digit()).when(accessClaims).getId();

        Mockito.doReturn(faker.internet().ipV4Address()).when(request).getRemoteAddr();

        taskContextHandlerInterceptor.preHandle(request, response, null);
        Mockito.verify(taskContextFactory, Mockito.times(1))
                .start(Mockito.argThat(task -> {
                    assertNotNull(task);

                    assertNotNull(task.getWebRequest());
                    WebRequest webRequest = task.getWebRequest();
                    assertEquals(accessClaims.getId(), webRequest.getSessionId());
                    assertEquals(accessClaims.getSubject(), webRequest.getUserId());
                    return true;
                }));
    }

    @Test
    void preHandleForProxy() {
        String ipV4Address = faker.internet().ipV4Address();
        taskContextHandlerInterceptor.setIP_V4_LOCALHOST("127.0.0.1");
        Mockito.doReturn(taskContextHandlerInterceptor.getIP_V4_LOCALHOST()).when(request).getRemoteAddr();
        Mockito.doReturn(ipV4Address).when(request).getHeader(Mockito.eq(taskContextHandlerInterceptor.getProxyIpHeader()));

        Mockito.doReturn(faker.internet().userAgentAny()).when(request).getHeader(Mockito.eq(HttpHeaders.USER_AGENT));
        taskContextHandlerInterceptor.preHandle(request, response, null);
        Mockito.verify(taskContextFactory, Mockito.times(1))
                .start(Mockito.argThat(task -> {
                    assertNotNull(task);
                    assertNotNull(task.getWebRequest());
                    WebRequest webRequest = task.getWebRequest();
                    assertEquals(ipV4Address, webRequest.getIpAddress());
                    return true;
                }));
    }

    @Test
    void afterCompletion() {
        Task task = new Task();
        task.setDuration(Duration.builder().startedOn(LocalDateTime.now()).build());
        task.setWebRequest(new WebRequest());
        TaskContextImpl taskContext = new TaskContextImpl(task, null, taskContextHolder, null);
        Mockito.doReturn(taskContext).when(taskContextHolder).getObject();
        int statusCode = faker.number().randomDigit();
        Mockito.doReturn(statusCode).when(response).getStatus();

        taskContextHandlerInterceptor.afterCompletion(request, response, null, null);
        Mockito.verify(taskContextSaver, Mockito.times(1)).save(taskContext);
        assertNotNull(task.getDuration().getNanoSecondsTaken());
        assertEquals(statusCode, task.getWebRequest().getStatusCode());
    }
}