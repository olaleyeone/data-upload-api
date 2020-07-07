package com.github.olaleyeone.dataupload.response.handler;

import com.github.olaleyeone.dataupload.data.dto.DataChunk;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.data.entity.DataUploadChunk;
import com.github.olaleyeone.dataupload.repository.DataUploadChunkRepository;
import com.github.olaleyeone.dataupload.test.component.ComponentTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RangeResponseHandlerTest extends ComponentTest {

    @Mock
    private DataUploadChunkRepository dataUploadChunkRepository;

    @InjectMocks
    private RangeResponseHandler rangeResponseHandler;

    private DataUpload dataUpload;

    @BeforeEach
    public void setUp() {
        dataUpload = new DataUpload();
        dataUpload.setContentType("text/plain");
    }

    @Test
    public void peekStart() throws Exception {

        List<DataUploadChunk> uploadChunks = getChunks(3);
        Long size = uploadChunks.stream().map(DataUploadChunk::getSize).reduce((a, b) -> a + b).get().longValue();
        dataUpload.setSize(size);

        int start = 0;
        int end = 1;

        validateRangeRequest(uploadChunks, start, end);
    }

    @Test
    public void getDataRange() throws Exception {

        List<DataUploadChunk> uploadChunks = getChunks(3);
        Long size = uploadChunks.stream().map(DataUploadChunk::getSize).reduce((a, b) -> a + b).get().longValue();
        dataUpload.setSize(size);

        int start = 0;
        int end = Long.valueOf(size / 2).intValue();

        validateRangeRequest(uploadChunks, start, end);
    }

    @Test
    public void getDataRange2() throws Exception {

        List<DataUploadChunk> uploadChunks = getChunks(5);
        Long size = uploadChunks.stream().map(DataUploadChunk::getSize).reduce((a, b) -> a + b).get().longValue();
        dataUpload.setSize(size);

        Iterator<DataUploadChunk> iterator = uploadChunks.iterator();
        DataUploadChunk first = iterator.next();
        DataUploadChunk second = iterator.next();
        int start = Long.valueOf(first.getStart() + first.getSize() - 1).intValue() + second.getSize() / 2;
        int end = size.intValue() - 1;

        validateRangeRequest(uploadChunks, start, end);
    }

    private void validateRangeRequest(List<DataUploadChunk> uploadChunks, int start, int end) {
        Mockito.doReturn(uploadChunks.stream()
                .map(dataUploadChunk -> new DataChunk(
                        dataUploadChunk.getId(),
                        dataUploadChunk.getStart(),
                        dataUploadChunk.getSize()))
                .collect(Collectors.toList()))
                .when(dataUploadChunkRepository).getChunks(Mockito.any(), Mockito.anyLong(), Mockito.anyLong());

        Mockito.doAnswer(invocation -> {
            DataUploadChunk dataUploadChunk = uploadChunks.stream()
                    .filter(it -> it.getId().equals(invocation.getArgument(0)))
                    .findFirst().get();
            int offset = invocation.getArgument(1);
            int size = invocation.getArgument(2);
            return Arrays.copyOfRange(dataUploadChunk.getData(), offset, offset + size);
        }).when(dataUploadChunkRepository).getData(Mockito.any(), Mockito.anyInt(), Mockito.anyInt());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        uploadChunks.forEach(dataUploadChunk -> {
            try {
                bos.write(dataUploadChunk.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        byte[] expected = Arrays.copyOfRange(bos.toByteArray(), start, end + 1);

        List<HttpRange> ranges = Collections.singletonList(HttpRange.createByteRange(start, end));
        ResponseEntity<ByteArrayResource> responseEntity = rangeResponseHandler.sendRange(dataUpload, ranges);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.PARTIAL_CONTENT, responseEntity.getStatusCode());
        assertArrayEquals(expected, responseEntity.getBody().getByteArray());
        int expectedContentLength = (end + 1) - start;
        assertEquals(expectedContentLength, responseEntity.getBody().getByteArray().length);
        assertEquals(expectedContentLength, responseEntity.getBody().contentLength());
    }

    public List<DataUploadChunk> getChunks(int numberOfChunks) {
        long start = 1L;
        List<DataUploadChunk> uploadChunks = new ArrayList<>();
        for (int i = 0; i < numberOfChunks; i++) {
            DataUploadChunk dataUploadChunk = new DataUploadChunk();
            dataUploadChunk.setId(i + 1L);
            dataUploadChunk.setStart(start);
            dataUploadChunk.setData(faker.backToTheFuture().quote().getBytes());
            dataUploadChunk.prePersist();
            uploadChunks.add(dataUploadChunk);

            start += dataUploadChunk.getSize();
        }
        return uploadChunks;
    }
}