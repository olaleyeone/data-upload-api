package com.github.olaleyeone.dataupload.test.dto;

import com.github.heywhy.springentityfactory.contracts.EntityFactoryBuilder;
import com.github.heywhy.springentityfactory.contracts.FactoryHelper;
import com.github.heywhy.springentityfactory.contracts.ModelFactory;
import com.github.heywhy.springentityfactory.impl.ModelFactoryImpl;
import com.github.javafaker.Faker;
import com.google.common.reflect.ClassPath;
import lombok.SneakyThrows;

import java.util.List;

public class DtoFactory {

    private final ModelFactory modelFactory;

    public DtoFactory(Faker faker) {
        this.modelFactory = new ModelFactoryImpl(faker);
        registerFactories(modelFactory);
    }

    @SneakyThrows
    public void registerFactories(ModelFactory modelFactory) {
        String className = getClass().getName();
        String packageName = className.substring(0, className.length() - (getClass().getSimpleName().length() + 1));
        ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive(packageName)
                .stream()
                .map(classInfo -> classInfo.load())
                .filter(javaClass -> FactoryHelper.class.isAssignableFrom(javaClass))
                .forEach(aClass -> modelFactory.register((Class) aClass));
    }

    public <T> T make(Class<T> model) {
        return modelFactory.make(model);
    }

    public <T> List<T> make(Class<T> model, int count) {
        return modelFactory.make(model, count);
    }

    public <T> EntityFactoryBuilder<T> pipe(Class<T> model) {
        return modelFactory.pipe(model);
    }
}
