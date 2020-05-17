package com.github.olaleyeone.dataupload.response.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MissingChunkApiResponse {

    private Long start;

    private Long size;
}
