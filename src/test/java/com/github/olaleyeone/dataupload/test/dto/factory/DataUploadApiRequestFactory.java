package com.github.olaleyeone.dataupload.test.dto.factory;

import com.github.heywhy.springentityfactory.contracts.FactoryHelper;
import com.github.heywhy.springentityfactory.contracts.ModelFactory;
import com.github.javafaker.Faker;
import com.github.olaleyeone.dataupload.data.dto.DataUploadApiRequest;

public class DataUploadApiRequestFactory implements FactoryHelper<DataUploadApiRequest> {

    @Override
    public Class<DataUploadApiRequest> getEntity() {
        return DataUploadApiRequest.class;
    }

    @Override
    public DataUploadApiRequest apply(Faker faker, ModelFactory factory) {
        DataUploadApiRequest apiRequest = new DataUploadApiRequest();
        apiRequest.setSize(faker.number().randomNumber());
//        apiRequest.setContentType("text/plain");
        apiRequest.setUserId(faker.idNumber().valid());
        return apiRequest;
    }
}
