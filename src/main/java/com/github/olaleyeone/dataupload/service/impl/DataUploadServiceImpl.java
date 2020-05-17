package com.github.olaleyeone.dataupload.service.impl;

import com.github.olaleyeone.dataupload.data.dto.DataUploadApiRequest;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.repository.DataUploadRepository;
import com.github.olaleyeone.dataupload.service.api.DataUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.util.Optional;

@RequiredArgsConstructor
@Service
class DataUploadServiceImpl implements DataUploadService {

    private final DataUploadRepository dataUploadRepository;

    @Transactional
    @Override
    public DataUpload createDataUpload(DataUploadApiRequest dto) {
        DataUpload dataUpload = new DataUpload();
//        dataUpload.setContentType(dto.getContentType());
        dataUpload.setSize(dto.getSize());
        dataUpload.setUserId(dto.getUserId());
        Optional.ofNullable(dto.getDescription()).filter(StringUtils::hasText)
                .map(StringUtils::trimWhitespace).ifPresent(dataUpload::setDescription);
        dataUploadRepository.save(dataUpload);
        return dataUpload;
    }
}
