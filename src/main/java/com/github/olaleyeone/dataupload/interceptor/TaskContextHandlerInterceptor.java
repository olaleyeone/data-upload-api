package com.github.olaleyeone.dataupload.interceptor;

import com.github.olaleyeone.auth.data.AuthorizedRequest;
import com.olaleyeone.audittrail.embeddable.Duration;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.WebRequest;
import com.olaleyeone.audittrail.impl.TaskContextFactory;
import com.olaleyeone.audittrail.impl.TaskContextHolder;
import com.olaleyeone.audittrail.impl.TaskContextImpl;
import com.olaleyeone.audittrail.impl.TaskContextSaver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
public class TaskContextHandlerInterceptor extends HandlerInterceptorAdapter {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Getter
    @Setter
    @Value("${IP_V4_LOCALHOST:127.0.0.1}")
    private String IP_V4_LOCALHOST;
    @Value("${IP_V6_LOCALHOST:0:0:0:0:0:0:0:1}")
    private String IP_V6_LOCALHOST;

    @Getter
    @Setter
    private String proxyIpHeader = "X-FORWARDED-FOR";

    private final TaskContextFactory taskContextFactory;

    private final TaskContextHolder taskContextHolder;

    private final TaskContextSaver taskContextSaver;

    private final Provider<AuthorizedRequest> authorizedRequestProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Task task = new Task();
        task.setName(request.getServletPath());
        task.setType(Task.WEB_REQUEST);
        task.setDuration(new Duration(LocalDateTime.now(), null));

        WebRequest webRequest = new WebRequest();
        webRequest.setIpAddress(getActualIpAddress(request));
        webRequest.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
        webRequest.setUri(request.getRequestURI());
        try {
            AuthorizedRequest authorizedRequest = authorizedRequestProvider.get();
            if (authorizedRequest.getAccessClaims() != null) {
                webRequest.setUserId(authorizedRequest.getAccessClaims().getSubject());
                webRequest.setSessionId(authorizedRequest.getAccessClaims().getId());
            }
        } catch (Exception e) {

        }
        task.setWebRequest(webRequest);

        taskContextFactory.start(task);
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            @Nullable Exception ex) {
        TaskContextImpl taskContext = taskContextHolder.getObject();
        Task task = taskContext.getTask();
        WebRequest webRequest = task.getWebRequest();
        webRequest.setStatusCode(response.getStatus());

        Duration duration = task.getDuration();
        duration.setNanoSecondsTaken(duration.getStartedOn().until(LocalDateTime.now(), ChronoUnit.NANOS));
        taskContextSaver.save(taskContext);
    }

    public String getActualIpAddress(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        if (isWebServer(ipAddress) && StringUtils.isNotBlank(request.getHeader(proxyIpHeader))) {
            ipAddress = request.getHeader(proxyIpHeader);
        }
        return ipAddress;
    }

    public boolean isWebServer(String ipAddress) {
        return isLocalhost(ipAddress);
    }

    public boolean isLocalhost(String ipAddress) {
        return ipAddress.equals(IP_V4_LOCALHOST) || ipAddress.equals(IP_V6_LOCALHOST);
    }
}
