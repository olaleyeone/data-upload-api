package com.github.olaleyeone.dataupload.test.component;


import com.github.heywhy.springentityfactory.contracts.ModelFactory;
import com.github.javafaker.Faker;
import com.github.olaleyeone.dataupload.test.dto.DtoFactory;
import com.github.olaleyeone.dataupload.test.entity.EntityFactoryConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Random;

@ExtendWith(MockitoExtension.class)
public class ComponentTest {

    protected final Faker faker = Faker.instance(new Random());

    protected final DtoFactory dtoFactory = new DtoFactory(faker);
    protected final ModelFactory modelFactory = new EntityFactoryConfiguration().entityFactory(faker, null);
}
