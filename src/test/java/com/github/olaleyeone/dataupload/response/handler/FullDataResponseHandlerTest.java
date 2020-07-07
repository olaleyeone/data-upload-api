package com.github.olaleyeone.dataupload.response.handler;

import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.data.entity.DataUploadChunk;
import com.github.olaleyeone.dataupload.repository.DataUploadChunkRepository;
import com.github.olaleyeone.dataupload.test.component.ComponentTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class FullDataResponseHandlerTest extends ComponentTest {

    @Mock
    private DataUploadChunkRepository dataUploadChunkRepository;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private ServletOutputStream servletOutputStream;

    @InjectMocks
    private FullDataResponseHandler responseHandler;

    private DataUpload dataUpload;

    @BeforeEach
    public void setUp() {
        dataUpload = new DataUpload();
        dataUpload.setContentType("text/plain");
    }

    @Test
    public void getData() throws Exception {
        DataUpload dataUpload = new DataUpload();
        dataUpload.setSize(faker.number().randomNumber());

        Mockito.doReturn(servletOutputStream).when(httpServletResponse).getOutputStream();

        long start = faker.number().randomDigit();
        List<Long> ids = Arrays.asList(start++, start++, start++);
        Mockito.doReturn(ids).when(dataUploadChunkRepository).getChunkIds(Mockito.any());
        List<DataUploadChunk> uploadChunks = getChunks(ids.size());
        Mockito.doAnswer(invocation -> Optional.of(uploadChunks.get(ids.indexOf(invocation.getArgument(0)))))
                .when(dataUploadChunkRepository).findById(Mockito.any());

        responseHandler.sendAll(dataUpload, httpServletResponse);

        InOrder inOrder = Mockito.inOrder(servletOutputStream);
        uploadChunks.forEach(dataUploadChunk -> {
            try {
                inOrder.verify(servletOutputStream, Mockito.times(1)).write(dataUploadChunk.getData());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private List<DataUploadChunk> getChunks(int numberOfChunks) {
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