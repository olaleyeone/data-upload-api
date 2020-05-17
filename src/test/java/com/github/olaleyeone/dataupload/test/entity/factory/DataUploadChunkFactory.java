package com.github.olaleyeone.dataupload.test.entity.factory;

import com.github.heywhy.springentityfactory.contracts.FactoryHelper;
import com.github.heywhy.springentityfactory.contracts.ModelFactory;
import com.github.javafaker.Faker;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.data.entity.DataUploadChunk;

public class DataUploadChunkFactory implements FactoryHelper<DataUploadChunk> {

    @Override
    public Class<DataUploadChunk> getEntity() {
        return DataUploadChunk.class;
    }

    @Override
    public DataUploadChunk apply(Faker faker, ModelFactory factory) {
        DataUploadChunk dataUploadChunk = new DataUploadChunk();
        dataUploadChunk.setStart(1L);
        dataUploadChunk.setData(faker.backToTheFuture().quote().getBytes());
        dataUploadChunk.setDataUpload(factory.create(DataUpload.class));
        return dataUploadChunk;
    }
}
