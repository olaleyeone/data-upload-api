package com.github.olaleyeone.dataupload.repository;

import com.github.olaleyeone.dataupload.data.dto.DataChunk;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.data.entity.DataUploadChunk;
import com.github.olaleyeone.dataupload.test.entity.EntityTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataUploadChunkRepositoryTest extends EntityTest {

    @Autowired
    private DataUploadChunkRepository dataUploadChunkRepository;

    @Test
    void searchChunkInRange() {
        DataUploadChunk dataUploadChunk = modelFactory.create(DataUploadChunk.class);
        assertNotNull(dataUploadChunk);
        assertNotNull(dataUploadChunk.getId());

        int count = dataUploadChunkRepository.countByRange(dataUploadChunk.getDataUpload(),
                dataUploadChunk.getStart(), dataUploadChunk.getSize());
        assertTrue(count > 0);
    }

    @Test
    void searchChunkAboveRange() {
        DataUploadChunk dataUploadChunk = modelFactory.create(DataUploadChunk.class);

        int count = dataUploadChunkRepository.countByRange(dataUploadChunk.getDataUpload(),
                dataUploadChunk.getStart() + dataUploadChunk.getSize(), 1);
        assertTrue(count == 0);
    }

    @Test
    void searchChunkBelowRange() {
        DataUploadChunk dataUploadChunk = modelFactory.create(DataUploadChunk.class);

        int count = dataUploadChunkRepository.countByRange(dataUploadChunk.getDataUpload(),
                dataUploadChunk.getStart() - 1, 1);
        assertTrue(count == 0);
    }

    @Test
    void searchChunkWithOverlap() {
        DataUploadChunk dataUploadChunk = modelFactory.create(DataUploadChunk.class);

        int count = dataUploadChunkRepository.countByRange(dataUploadChunk.getDataUpload(),
                dataUploadChunk.getStart() - 1, 2);
        assertTrue(count > 0);
    }

    @Test
    void searchChunkWithOverlap2() {
        DataUploadChunk dataUploadChunk = modelFactory.create(DataUploadChunk.class);

        int count = dataUploadChunkRepository.countByRange(dataUploadChunk.getDataUpload(),
                dataUploadChunk.getStart() + dataUploadChunk.getSize() - 1, 2);
        assertTrue(count > 0);
    }

    @Test
    void getOpenChunks() {
        DataUpload dataUpload = modelFactory.pipe(DataUpload.class)
                .then(it -> {
                    it.setSize(100L);
                    return it;
                }).create();
        List<Long> openEntries = loadChunks(dataUpload);
        List<DataChunk> openChunks = dataUploadChunkRepository.getOpenChunks(dataUpload);
        openChunks.stream().map(DataChunk::getStart).forEach(openEntries::remove);
        assertTrue(openEntries.isEmpty());
    }

    private List<Long> loadChunks(DataUpload dataUpload) {
        List<Long> open = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int index = i;
            modelFactory.pipe(DataUploadChunk.class)
                    .then(dataUploadChunk -> {
                        dataUploadChunk.setDataUpload(dataUpload);
                        dataUploadChunk.setStart(Long.valueOf((index * 10) + 1));
                        byte[] data;
                        if (index < 5) {
                            data = new byte[10];
                        } else {
                            data = new byte[5];
                            open.add(dataUploadChunk.getStart());
                        }
                        Arrays.fill(data, Byte.MAX_VALUE);
                        dataUploadChunk.setData(data);
                        return dataUploadChunk;
                    })
                    .create();
        }
        return open;
    }
}