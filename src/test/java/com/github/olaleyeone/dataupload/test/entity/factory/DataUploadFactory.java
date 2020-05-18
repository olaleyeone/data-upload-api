package com.github.olaleyeone.dataupload.test.entity.factory;

import com.github.heywhy.springentityfactory.contracts.FactoryHelper;
import com.github.heywhy.springentityfactory.contracts.ModelFactory;
import com.github.javafaker.Faker;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;

public class DataUploadFactory implements FactoryHelper<DataUpload> {

    @Override
    public Class<DataUpload> getEntity() {
        return DataUpload.class;
    }

    @Override
    public DataUpload apply(Faker faker, ModelFactory factory) {
        DataUpload dataUpload = new DataUpload();
//        dataUpload.setSize(faker.number().randomNumber());
//        dataUpload.setContentType("text/plain");
        dataUpload.setUserId(faker.idNumber().valid());
        return dataUpload;
    }
}
