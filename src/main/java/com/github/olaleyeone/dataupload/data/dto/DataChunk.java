package com.github.olaleyeone.dataupload.data.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Builder
@Data
public class DataChunk {

    private final Long start;
    private final Integer size;

    public Long getStartOfNextChunk() {
        return start + size;
    }
}
