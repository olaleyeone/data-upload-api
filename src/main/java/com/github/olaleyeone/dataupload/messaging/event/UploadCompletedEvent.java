package com.github.olaleyeone.dataupload.messaging.event;

import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class UploadCompletedEvent {

    private final DataUpload dataUpload;
}
