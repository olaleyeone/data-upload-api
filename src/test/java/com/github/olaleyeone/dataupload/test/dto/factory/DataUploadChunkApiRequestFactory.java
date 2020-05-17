package com.github.olaleyeone.dataupload.test.dto.factory;

import com.github.heywhy.springentityfactory.contracts.FactoryHelper;
import com.github.heywhy.springentityfactory.contracts.ModelFactory;
import com.github.javafaker.Faker;
import com.github.olaleyeone.dataupload.data.dto.DataUploadChunkApiRequest;

public class DataUploadChunkApiRequestFactory implements FactoryHelper<DataUploadChunkApiRequest> {

    @Override
    public Class<DataUploadChunkApiRequest> getEntity() {
        return DataUploadChunkApiRequest.class;
    }

    @Override
    public DataUploadChunkApiRequest apply(Faker faker, ModelFactory factory) {
        DataUploadChunkApiRequest apiRequest = new DataUploadChunkApiRequest();
        apiRequest.setStart(1L);
        apiRequest.setData(faker.backToTheFuture().quote().getBytes());
        return apiRequest;
    }
}
