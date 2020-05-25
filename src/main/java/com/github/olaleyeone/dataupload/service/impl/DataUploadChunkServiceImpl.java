package com.github.olaleyeone.dataupload.service.impl;

import com.github.olaleyeone.dataupload.data.dto.DataUploadChunkApiRequest;
import com.github.olaleyeone.dataupload.data.entity.DataUploadChunk;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.repository.DataUploadChunkRepository;
import com.github.olaleyeone.dataupload.repository.DataUploadRepository;
import com.github.olaleyeone.dataupload.service.api.DataUploadChunkService;
import com.olaleyeone.audittrail.api.Activity;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@RequiredArgsConstructor
@Service
public class DataUploadChunkServiceImpl implements DataUploadChunkService {

    private final DataUploadChunkRepository dataUploadChunkRepository;
    private final DataUploadRepository dataUploadRepository;

    @Activity("SAVE UPLOAD DATA")
    @Transactional
    @Override
    public DataUploadChunk createChunk(DataUpload dataUpload, DataUploadChunkApiRequest dto) {
        DataUploadChunk dataUploadChunk = new DataUploadChunk();
        dataUploadChunk.setDataUpload(dataUpload);
        dataUploadChunk.setStart(dto.getStart());
        dataUploadChunk.setData(dto.getData());
        dataUploadChunkRepository.save(dataUploadChunk);

        if (StringUtils.isBlank(dataUpload.getContentType())) {
            dataUpload.setContentType(dto.getContentType());
        }
        if (dataUpload.getSize() == null) {
            if (dto.getTotalSize() == null) {
                throw new IllegalArgumentException("Total upload size required");
            }
            dataUpload.setSize(dto.getTotalSize());
            dataUploadRepository.save(dataUpload);
        }
        return dataUploadChunk;
    }

    @Activity("DELETE UPLOAD DATA")
    @Transactional
    @Override
    public void delete(DataUploadChunk dataUploadChunk) {
        dataUploadChunkRepository.delete(dataUploadChunk);
    }
}
