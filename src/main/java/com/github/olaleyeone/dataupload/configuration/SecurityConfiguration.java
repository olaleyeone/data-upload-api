package com.github.olaleyeone.dataupload.configuration;

import com.github.olaleyeone.auth.BaseJwtService;
import com.github.olaleyeone.auth.SignatureKeySource;
import com.github.olaleyeone.auth.SignatureKeySourceImpl;
import com.github.olaleyeone.auth.SigningKeyResolverImpl;
import com.olaleyeone.auth.api.SignatureKeyControllerApi;
import com.olaleyeone.auth.security.access.AccessStatus;
import com.olaleyeone.auth.security.access.TrustedIpAddressAuthorizer;
import com.olaleyeone.auth.security.data.AccessClaimsExtractor;
import com.olaleyeone.auth.security.data.AuthorizedRequestFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Configuration
public class SecurityConfiguration {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    @Bean
    public AuthorizedRequestFactory requestMetadataFactory(HttpServletRequest httpServletRequest, AccessClaimsExtractor accessClaimsExtractor) {
        return new AuthorizedRequestFactory(httpServletRequest, accessClaimsExtractor) {

            private String tokenPrefix = "Bearer ";
            public String accessTokenCookieName = "access_token";

            @Override
            protected Optional<String> getAccessToken(HttpServletRequest httpServletRequest) {

                String authorizationHeader = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
                if (StringUtils.isNotBlank(authorizationHeader) && authorizationHeader.startsWith(tokenPrefix)) {
                    return Optional.of(authorizationHeader.substring(tokenPrefix.length()));
                }

                if (httpServletRequest.getCookies() == null) {
                    return Optional.empty();
                }

                return Arrays.asList(httpServletRequest.getCookies())
                        .stream()
                        .filter(cookie -> cookie.getName().equals(accessTokenCookieName))
                        .findFirst()
                        .map(Cookie::getValue);
            }
        };
    }

    @Profile("!test")
    @Bean
    public TrustedIpAddressAuthorizer trustedIpAddressAccessManager() {
        return (accessConstraint, ipAddress) -> {
//            Optional<String> value = settingService.getString(StringUtils.defaultIfBlank(accessConstraint.value(), "TRUSTED_IP"));
//            if (value.isPresent()) {
//                return Arrays.asList(value.get().split(" *, *")).contains(ipAddress)
//                        ? AccessStatus.allowed()
//                        : AccessStatus.denied(ipAddress);
//            }
//            if (accessConstraint.defaultIpAddresses().length > 0) {
//                return Arrays.asList(accessConstraint.defaultIpAddresses()).contains(ipAddress)
//                        ? AccessStatus.allowed()
//                        : AccessStatus.denied(ipAddress);
//            }
            return AccessStatus.denied("");
        };
    }

    @Profile("!test")
    @Bean
    public AccessClaimsExtractor accessTokenValidator(BaseJwtService jwtService) {
        return token -> {
            try {
                return jwtService.parseAccessToken(token);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return null;
            }
        };
    }

    @Bean
    public BaseJwtService baseJwtService() {
        return beanFactory.createBean(BaseJwtService.class);
    }

    @Bean
    public SigningKeyResolverImpl signingKeyResolver(SignatureKeySource signatureKeySource) {
        return new SigningKeyResolverImpl(signatureKeySource);
    }

    @Bean
    public SignatureKeySource signatureKeySource(SignatureKeyControllerApi signatureKeyControllerApi) {
        return new SignatureKeySourceImpl(signatureKeyControllerApi);
    }
}
