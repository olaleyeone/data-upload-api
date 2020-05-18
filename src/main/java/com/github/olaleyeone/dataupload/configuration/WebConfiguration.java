package com.github.olaleyeone.dataupload.configuration;

import com.github.olaleyeone.dataupload.interceptor.TaskContextHandlerInterceptor;
import com.olaleyeone.auth.security.interceptors.AccessConstraintHandlerInterceptor;
import org.springdoc.webmvc.api.OpenApiResource;
import org.springdoc.webmvc.ui.SwaggerWelcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
@ComponentScan({
        "com.github.olaleyeone.dataupload.controller",
        "com.github.olaleyeone.dataupload.advice",
        "com.github.olaleyeone.dataupload.validator",
        "com.github.olaleyeone.dataupload.response.handler"
})
@Import({
        AdditionalComponentsConfiguration.class,
        BeanValidationConfiguration.class,
        OpenApiConfiguration.class
})
public class WebConfiguration implements WebMvcConfigurer {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
        registry.addInterceptor(beanFactory.createBean(TaskContextHandlerInterceptor.class));
//        registry.addInterceptor(beanFactory.createBean(RemoteAddressConstraintHandlerInterceptor.class));
        AccessConstraintHandlerInterceptor accessConstraintHandlerInterceptor = new AccessConstraintHandlerInterceptor(
                applicationContext,
                Arrays.asList(BasicErrorController.class,
                        OpenApiResource.class,
                        SwaggerWelcome.class)
        );
        beanFactory.autowireBean(accessConstraintHandlerInterceptor);
        registry.addInterceptor(accessConstraintHandlerInterceptor);
    }
}
