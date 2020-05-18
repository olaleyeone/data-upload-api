package com.github.olaleyeone.dataupload.event.mesage;

import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class UploadCompleteEvent {

    private final DataUpload dataUpload;
}
