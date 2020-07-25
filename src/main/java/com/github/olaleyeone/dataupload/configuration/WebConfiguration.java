package com.github.olaleyeone.dataupload.configuration;

import com.github.olaleyeone.advice.ErrorAdvice;
import com.github.olaleyeone.auth.data.AuthorizedRequest;
import com.github.olaleyeone.auth.interceptors.AccessConstraintHandlerInterceptor;
import com.github.olaleyeone.configuration.BeanValidationConfiguration;
import com.github.olaleyeone.configuration.JacksonConfiguration;
import com.github.olaleyeone.configuration.PredicateConfiguration;
import com.github.olaleyeone.interceptor.TaskContextHandlerInterceptor;
import org.springdoc.webmvc.api.OpenApiResource;
import org.springdoc.webmvc.ui.SwaggerWelcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.inject.Provider;
import java.util.Arrays;

@Configuration
@ComponentScan({
        "com.github.olaleyeone.dataupload.controller",
        "com.github.olaleyeone.dataupload.validator",
        "com.github.olaleyeone.dataupload.response.handler"
})
@Import({
        RequestMetadataConfiguration.class,
        PredicateConfiguration.class,
        BeanValidationConfiguration.class,
        JacksonConfiguration.class,
        OpenApiConfiguration.class
})
public class WebConfiguration implements WebMvcConfigurer {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Provider<AuthorizedRequest> authorizedRequestProvider;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
        registry.addInterceptor(beanFactory.createBean(TaskContextHandlerInterceptor.class));

        AccessConstraintHandlerInterceptor accessConstraintHandlerInterceptor = new AccessConstraintHandlerInterceptor(
                applicationContext,
                authorizedRequestProvider,
                Arrays.asList(BasicErrorController.class,
                        OpenApiResource.class,
                        SwaggerWelcome.class)
        );
        beanFactory.autowireBean(accessConstraintHandlerInterceptor);
        registry.addInterceptor(accessConstraintHandlerInterceptor);
    }

    @Bean
    public ErrorAdvice errorAdvice() {
        return new ErrorAdvice();
    }
}
