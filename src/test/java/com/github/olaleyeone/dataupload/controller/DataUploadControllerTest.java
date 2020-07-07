package com.github.olaleyeone.dataupload.controller;

import com.github.olaleyeone.dataupload.data.dto.DataChunk;
import com.github.olaleyeone.dataupload.data.dto.DataUploadApiRequest;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.data.entity.DataUploadChunk;
import com.github.olaleyeone.dataupload.repository.DataUploadChunkRepository;
import com.github.olaleyeone.dataupload.repository.DataUploadRepository;
import com.github.olaleyeone.dataupload.response.handler.DataUploadApiResponseHandler;
import com.github.olaleyeone.dataupload.response.pojo.DataUploadApiResponse;
import com.github.olaleyeone.dataupload.service.api.DataUploadService;
import com.github.olaleyeone.dataupload.test.controller.ControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DataUploadControllerTest extends ControllerTest {

    @Autowired
    private DataUploadService dataUploadService;

    @Autowired
    private DataUploadRepository dataUploadRepository;

    @Autowired
    private DataUploadChunkRepository dataUploadChunkRepository;

    @Autowired
    private DataUploadApiResponseHandler dataUploadApiResponseHandler;

    @Test
    void createDataUpload() throws Exception {
        DataUpload dataUpload = new DataUpload();
        dataUpload.setId(faker.number().randomNumber());
        Mockito.doReturn(dataUpload).when(dataUploadService).createDataUpload(Mockito.any());
        DataUploadApiRequest apiRequest = dtoFactory.make(DataUploadApiRequest.class);
        mockMvc.perform(MockMvcRequestBuilders.post("/uploads")
                .with(body(apiRequest)))
                .andExpect(status().isCreated())
                .andExpect(result -> {
                    DataUploadApiResponse apiResponse = objectMapper.readValue(result.getResponse().getContentAsByteArray(), DataUploadApiResponse.class);
                    assertNotNull(apiResponse);
                    assertEquals(dataUpload.getId(), apiResponse.getId());
                });

        Mockito.verify(dataUploadService, Mockito.times(1))
                .createDataUpload(apiRequest);
    }

    @Test
    public void getDetails() throws Exception {
        DataUpload dataUpload = new DataUpload();
        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());
        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}", faker.number().randomDigit()))
                .andExpect(status().isOk());
        Mockito.verify(dataUploadApiResponseHandler, Mockito.times(1))
                .getDataUploadApiResponse(dataUpload);
    }

    @Test
    public void handleDetailsNotFound() throws Exception {
        Mockito.doReturn(Optional.empty()).when(dataUploadRepository).findById(Mockito.any());
        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}", faker.number().randomDigit()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getDataForFreshRecord() throws Exception {
        DataUpload dataUpload = new DataUpload();
        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());
        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}/data", faker.number().randomDigit()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getDataForIncompleteUpload() throws Exception {
        DataUpload dataUpload = new DataUpload();
        dataUpload.setSize(faker.number().randomNumber());
        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());
        Mockito.doReturn(dataUpload.getSize() / 2).when(dataUploadChunkRepository).sumData(Mockito.any());
        long id = faker.number().randomDigit();
        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}/data", id))
                .andExpect(status().isNotFound());
        Mockito.verify(dataUploadRepository, Mockito.times(1))
                .findById(id);
        Mockito.verify(dataUploadChunkRepository, Mockito.times(1))
                .sumData(dataUpload);
    }

    @Test
    public void getData() throws Exception {
        DataUpload dataUpload = new DataUpload();
        dataUpload.setSize(faker.number().randomNumber());
        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());
        Mockito.doReturn(dataUpload.getSize()).when(dataUploadChunkRepository).sumData(Mockito.any());

        long start = faker.number().randomDigit();
        List<Long> ids = Arrays.asList(start++, start++, start++);
        Mockito.doReturn(ids).when(dataUploadChunkRepository).getChunkIds(Mockito.any());
        List<DataUploadChunk> uploadChunks = getChunks(ids.size());
        Mockito.doAnswer(invocation -> Optional.of(uploadChunks.get(ids.indexOf(invocation.getArgument(0)))))
                .when(dataUploadChunkRepository).findById(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}/data", faker.number().randomDigit()))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    uploadChunks.forEach(dataUploadChunk -> {
                        try {
                            bos.write(dataUploadChunk.getData());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    assertArrayEquals(bos.toByteArray(), result.getResponse().getContentAsByteArray());
                });
    }

    @Test
    public void getDataRange() throws Exception {
        DataUpload dataUpload = new DataUpload();
        dataUpload.setContentType("text/plain");

        List<DataUploadChunk> uploadChunks = getChunks(3);
        Long size = uploadChunks.stream().map(DataUploadChunk::getSize).reduce((a, b) -> a + b).get().longValue();
        dataUpload.setSize(size);

        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());
        Mockito.doReturn(dataUpload.getSize()).when(dataUploadChunkRepository).sumData(Mockito.any());

        int start = 0;
        int end = Long.valueOf(size / 2).intValue();

        validateRangeRequest(uploadChunks, start, end);
    }

    @Test
    public void getDataRange2() throws Exception {
        DataUpload dataUpload = new DataUpload();
        dataUpload.setContentType("text/plain");

        List<DataUploadChunk> uploadChunks = getChunks(5);
        Long size = uploadChunks.stream().map(DataUploadChunk::getSize).reduce((a, b) -> a + b).get().longValue();
        dataUpload.setSize(size);

        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());
        Mockito.doReturn(dataUpload.getSize()).when(dataUploadChunkRepository).sumData(Mockito.any());

        Iterator<DataUploadChunk> iterator = uploadChunks.iterator();
        DataUploadChunk first = iterator.next();
        DataUploadChunk second = iterator.next();
        int start = Long.valueOf(first.getStart() + first.getSize() - 1).intValue() + second.getSize() / 2;
        int end = size.intValue() - 1;

        validateRangeRequest(uploadChunks, start, end);
    }

    private void validateRangeRequest(List<DataUploadChunk> uploadChunks, int start, int end) throws Exception {
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

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setRange(Collections.singletonList(HttpRange.createByteRange(start, end)));
        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}/data", faker.number().randomDigit())
                .headers(httpHeaders))
                .andExpect(status().isPartialContent())
                .andExpect(result -> assertArrayEquals(expected, result.getResponse().getContentAsByteArray()));
    }

    @Test
    public void getDataWithMissingChunk() throws Exception {
        DataUpload dataUpload = new DataUpload();
        dataUpload.setSize(faker.number().randomNumber());
        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());
        Mockito.doReturn(dataUpload.getSize()).when(dataUploadChunkRepository).sumData(Mockito.any());

        long start = faker.number().randomDigit();
        List<Long> ids = Arrays.asList(start++, start++, start++);
        Mockito.doReturn(ids).when(dataUploadChunkRepository).getChunkIds(Mockito.any());
        Mockito.doAnswer(invocation -> Optional.empty())
                .when(dataUploadChunkRepository).findById(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}/data", faker.number().randomDigit()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void getDataWithError() throws Exception {
        DataUpload dataUpload = new DataUpload();
        dataUpload.setSize(faker.number().randomNumber());
        Mockito.doReturn(Optional.of(dataUpload)).when(dataUploadRepository).findById(Mockito.any());
        Mockito.doReturn(dataUpload.getSize()).when(dataUploadChunkRepository).sumData(Mockito.any());

        long start = faker.number().randomDigit();
        List<Long> ids = Arrays.asList(start++, start++, start++);
        Mockito.doReturn(ids).when(dataUploadChunkRepository).getChunkIds(Mockito.any());
        DataUploadChunk uploadChunk = Mockito.mock(DataUploadChunk.class);
        Mockito.doAnswer(invocation -> {
            throw new IOException();
        }).when(uploadChunk).getData();
        Mockito.doAnswer(invocation -> Optional.of(uploadChunk))
                .when(dataUploadChunkRepository).findById(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.get("/uploads/{id}/data", faker.number().randomDigit()))
                .andExpect(status().isInternalServerError());
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