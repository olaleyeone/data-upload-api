package com.github.olaleyeone.dataupload.configuration;

import com.github.olaleyeone.auth.data.AuthorizedRequest;
import com.github.olaleyeone.dataupload.data.dto.RequestMetadata;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.request.RequestContextHolder;

import javax.inject.Provider;
import java.util.Optional;

@Configuration
public class RequestMetadataConfiguration {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RequestMetadata requestMetadata(Provider<AuthorizedRequest> requestMetadataProvider) {
        if (RequestContextHolder.getRequestAttributes() == null) {
            return null;
        }
        AuthorizedRequest authorizedRequest = requestMetadataProvider.get();

        RequestMetadata requestMetadata = new RequestMetadata();
        requestMetadata.setIpAddress(authorizedRequest.getIpAddress());
        requestMetadata.setUserAgent(authorizedRequest.getUserAgent());

        if (authorizedRequest.getAccessClaims() != null) {
            Optional.ofNullable(authorizedRequest.getAccessClaims().getSubject())
                    .ifPresent(requestMetadata::setPortalUserId);
            Optional.ofNullable(authorizedRequest.getAccessClaims().getId())
                    .ifPresent(requestMetadata::setRefreshTokenId);
        }
        return requestMetadata;
    }
}
