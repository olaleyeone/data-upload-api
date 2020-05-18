package com.github.olaleyeone.dataupload.data.dto;

import lombok.Data;

@Data
public class RequestMetadata {

    private String portalUserId;
    private String refreshTokenId;
    private String ipAddress;
    private String userAgent;
}
