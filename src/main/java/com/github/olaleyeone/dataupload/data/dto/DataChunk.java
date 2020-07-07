package com.github.olaleyeone.dataupload.data.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Builder
@Data
public class DataChunk {

    private final Long id;
    private final Long start;
    private final Integer size;

    public DataChunk(Long start, Integer size) {
        id = null;
        this.start = start;
        this.size = size;
    }

    public Long getStartOfNextChunk() {
        return start + size;
    }
}
