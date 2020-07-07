package com.github.olaleyeone.dataupload.response.handler;

import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.data.entity.DataUploadChunk;
import com.github.olaleyeone.dataupload.repository.DataUploadChunkRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Component
public class FullDataResponseHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final DataUploadChunkRepository dataUploadChunkRepository;

    public void sendAll(DataUpload dataUpload, HttpServletResponse httpServletResponse) throws IOException {

        httpServletResponse.setContentLength(dataUpload.getSize().intValue());
        httpServletResponse.setContentType(dataUpload.getContentType());
        List<Long> chunkIds = dataUploadChunkRepository.getChunkIds(dataUpload);
        for (Long id : chunkIds) {
            DataUploadChunk dataUploadChunk = dataUploadChunkRepository.findById(id).get();
            httpServletResponse.getOutputStream().write(dataUploadChunk.getData());
        }
    }
}
