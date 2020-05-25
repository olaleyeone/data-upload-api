package com.github.olaleyeone.dataupload.response.handler;

import com.github.olaleyeone.dataupload.data.dto.DataChunk;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.repository.DataUploadChunkRepository;
import com.github.olaleyeone.dataupload.response.pojo.DataUploadApiResponse;
import com.github.olaleyeone.dataupload.response.pojo.MissingChunkApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
public class DataUploadApiResponseHandler {

    private final DataUploadChunkRepository dataUploadChunkRepository;

    public DataUploadApiResponse getDataUploadApiResponse(DataUpload dataUpload) {
        DataUploadApiResponse apiResponse = new DataUploadApiResponse(dataUpload);
        apiResponse.setSizeUploaded(dataUploadChunkRepository.sumData(dataUpload));
        if (apiResponse.getSizeUploaded() == 0) {
            apiResponse.setMissingChunks(Collections.singletonList(MissingChunkApiResponse.builder()
                    .start(1L)
                    .size(dataUpload.getSize())
                    .build()));
        } else if (apiResponse.getSizeUploaded() < dataUpload.getSize()) {
            apiResponse.setMissingChunks(getMissingChunks(dataUpload));
        } else {
            apiResponse.setMissingChunks(Collections.EMPTY_LIST);
        }

        return apiResponse;
    }

    protected List<MissingChunkApiResponse> getMissingChunks(DataUpload dataUpload) {
        List<MissingChunkApiResponse> missingChunkApiResponses = new ArrayList<>();
        List<DataChunk> openChunks = dataUploadChunkRepository.getOpenChunks(dataUpload);
        for (int i = 0; i < openChunks.size(); i++) {
            DataChunk dataChunk = openChunks.get(i);
            if (i == 0 && dataChunk.getStart() > 1) {
                MissingChunkApiResponse missingChunkApiResponse = new MissingChunkApiResponse();
                missingChunkApiResponse.setStart(1L);
                missingChunkApiResponse.setSize(dataChunk.getStart() - 1);
                missingChunkApiResponses.add(missingChunkApiResponse);
            }

            if (dataChunk.getStartOfNextChunk() > dataUpload.getSize()) {
                break;
            }
            MissingChunkApiResponse missingChunkApiResponse = new MissingChunkApiResponse();
            missingChunkApiResponse.setStart(dataChunk.getStartOfNextChunk());
            if (i < openChunks.size() - 1) {
                missingChunkApiResponse.setSize(openChunks.get(i + 1).getStart() - dataChunk.getStartOfNextChunk());
            } else {
                missingChunkApiResponse.setSize((dataUpload.getSize() - dataChunk.getStartOfNextChunk()) + 1);
            }
            missingChunkApiResponses.add(missingChunkApiResponse);
        }
        return missingChunkApiResponses;
    }
}
