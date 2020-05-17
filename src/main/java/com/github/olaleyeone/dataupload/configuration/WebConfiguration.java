package com.github.olaleyeone.dataupload.configuration;

import com.github.olaleyeone.dataupload.interceptor.TaskContextHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan({
        "com.github.olaleyeone.dataupload.controller",
        "com.github.olaleyeone.dataupload.advice",
        "com.github.olaleyeone.dataupload.validator"
})
@Import({
        BeanValidationConfiguration.class
})
public class WebConfiguration implements WebMvcConfigurer {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
        registry.addInterceptor(beanFactory.createBean(TaskContextHandlerInterceptor.class));
    }
}
