package com.github.olaleyeone.dataupload.response.handler;

import com.github.olaleyeone.dataupload.data.dto.DataChunk;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.repository.DataUploadChunkRepository;
import com.github.olaleyeone.dataupload.response.pojo.DataUploadApiResponse;
import com.github.olaleyeone.dataupload.response.pojo.MissingChunkApiResponse;
import com.github.olaleyeone.dataupload.test.component.ComponentTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class DataUploadApiResponseHandlerTest extends ComponentTest {

    @Mock
    private DataUploadChunkRepository dataUploadChunkRepository;

    @InjectMocks
    private DataUploadApiResponseHandler dataUploadApiResponseHandler;

    private DataUpload dataUpload;

    @BeforeEach
    public void setUp() {
        dataUpload = modelFactory.make(DataUpload.class);
        dataUpload.setId(faker.number().randomNumber());
        dataUpload.setSize(faker.number().randomNumber());
    }

    @Test
    void getDataUploadApiResponse() {
        Mockito.doReturn(0L).when(dataUploadChunkRepository).sumData(Mockito.any());
        DataUploadApiResponse dataUploadApiResponse = dataUploadApiResponseHandler.getDataUploadApiResponse(dataUpload);
        assertNotNull(dataUploadApiResponse);
        assertNotNull(dataUploadApiResponse.getMissingChunks());
        assertEquals(1, dataUploadApiResponse.getMissingChunks().size());
        MissingChunkApiResponse missingChunkApiResponse = dataUploadApiResponse.getMissingChunks().iterator().next();
        assertEquals(1, missingChunkApiResponse.getStart());
        assertEquals(dataUpload.getSize(), missingChunkApiResponse.getSize());
        Mockito.verify(dataUploadChunkRepository, Mockito.times(1)).sumData(dataUpload);
        Mockito.verify(dataUploadChunkRepository, Mockito.never()).getOpenChunks(Mockito.any());
    }

    @Test
    void getMissingChunksAfterFullUpload() {
        Mockito.doReturn(dataUpload.getSize()).when(dataUploadChunkRepository).sumData(Mockito.any());
        DataUploadApiResponse dataUploadApiResponse = dataUploadApiResponseHandler.getDataUploadApiResponse(dataUpload);
        assertNotNull(dataUploadApiResponse);
        assertNotNull(dataUploadApiResponse.getMissingChunks());
        assertTrue(dataUploadApiResponse.getMissingChunks().isEmpty());
        Mockito.verify(dataUploadChunkRepository, Mockito.times(1)).sumData(dataUpload);
        Mockito.verify(dataUploadChunkRepository, Mockito.never()).getOpenChunks(Mockito.any());
    }

    @Test
    void getMissingChunksWithChunkAtStart() {
        dataUpload.setSize(100L);
        DataChunk dataChunk = DataChunk.builder()
                .start(1L)
                .size(10)
                .build();
        Mockito.doReturn(dataChunk.getSize().longValue()).when(dataUploadChunkRepository).sumData(Mockito.any());
        Mockito.doReturn(Collections.singletonList(dataChunk)).when(dataUploadChunkRepository).getOpenChunks(Mockito.any());
        DataUploadApiResponse dataUploadApiResponse = dataUploadApiResponseHandler.getDataUploadApiResponse(dataUpload);
        assertNotNull(dataUploadApiResponse);
        assertNotNull(dataUploadApiResponse.getMissingChunks());
        assertEquals(1, dataUploadApiResponse.getMissingChunks().size());

        MissingChunkApiResponse missingChunkApiResponse = dataUploadApiResponse.getMissingChunks().iterator().next();
        assertEquals(11, missingChunkApiResponse.getStart());
        assertEquals(90L, missingChunkApiResponse.getSize());
    }

    @Test
    void getMissingChunksWithChunkAtEnd() {
        dataUpload.setSize(100L);
        DataChunk dataChunk = DataChunk.builder()
                .start(91L)
                .size(10)
                .build();
        Mockito.doReturn(dataChunk.getSize().longValue()).when(dataUploadChunkRepository).sumData(Mockito.any());
        Mockito.doReturn(Collections.singletonList(dataChunk)).when(dataUploadChunkRepository).getOpenChunks(Mockito.any());
        DataUploadApiResponse dataUploadApiResponse = dataUploadApiResponseHandler.getDataUploadApiResponse(dataUpload);
        assertNotNull(dataUploadApiResponse);
        assertNotNull(dataUploadApiResponse.getMissingChunks());
        assertEquals(1, dataUploadApiResponse.getMissingChunks().size());

        MissingChunkApiResponse missingChunkApiResponse = dataUploadApiResponse.getMissingChunks().iterator().next();
        assertEquals(1, missingChunkApiResponse.getStart());
        assertEquals(90L, missingChunkApiResponse.getSize());
    }

    @Test
    void getMissingChunksWithChunks() {
        dataUpload.setSize(100L);
        DataChunk dataChunkAtStart = DataChunk.builder()
                .start(1L)
                .size(10)
                .build();
        DataChunk dataChunk1 = DataChunk.builder()
                .start(41L)
                .size(20)
                .build();
        DataChunk dataChunkAtEnd = DataChunk.builder()
                .start(91L)
                .size(10)
                .build();
        Mockito.doReturn(30L).when(dataUploadChunkRepository).sumData(Mockito.any());
        Mockito.doReturn(Arrays.asList(dataChunkAtStart, dataChunk1, dataChunkAtEnd)).when(dataUploadChunkRepository).getOpenChunks(Mockito.any());
        DataUploadApiResponse dataUploadApiResponse = dataUploadApiResponseHandler.getDataUploadApiResponse(dataUpload);
        assertNotNull(dataUploadApiResponse);
        assertNotNull(dataUploadApiResponse.getMissingChunks());
        assertEquals(2, dataUploadApiResponse.getMissingChunks().size());

        Iterator<MissingChunkApiResponse> iterator = dataUploadApiResponse.getMissingChunks().iterator();
        MissingChunkApiResponse missingChunkApiResponse1 = iterator.next();
        assertEquals(11, missingChunkApiResponse1.getStart());
        assertEquals(30L, missingChunkApiResponse1.getSize());

        MissingChunkApiResponse missingChunkApiResponse2 = iterator.next();
        assertEquals(61, missingChunkApiResponse2.getStart());
        assertEquals(30L, missingChunkApiResponse2.getSize());
    }
}