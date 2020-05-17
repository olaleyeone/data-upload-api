package com.github.olaleyeone.dataupload.configuration;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

@Configuration
public class BeanValidationConfiguration implements WebMvcConfigurer {

    @Bean
    @Override
    public LocalValidatorFactoryBean getValidator() {
        final LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean
                .setConstraintValidatorFactory(constraintValidatorFactory(null));
        return localValidatorFactoryBean;
    }

    @Bean
    public ConstraintValidatorFactory constraintValidatorFactory(AutowireCapableBeanFactory beanFactory) {
        return new ConstraintValidatorFactory() {

            @Override
            public void releaseInstance(
                    ConstraintValidator<?, ?> arg0) {
                beanFactory.destroyBean(arg0);
            }

            @Override
            public <T extends ConstraintValidator<?, ?>> T getInstance(
                    Class<T> arg0) {
                try {
                    return beanFactory.getBean(arg0);
                } catch (NoSuchBeanDefinitionException e) {
                    if (arg0.isInterface()) {
                        throw e;
                    }
                    return beanFactory.createBean(arg0);
                }
            }
        };
    }
}
