package com.aac.backend.infra.oauth;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oidc.google")
@Getter
public class GoogleOAuthProperties {

    private final String clientId;
    private final String clientSecret;
    private final String authorizationUri;
    private final String tokenUri;
    private final String userUri;
    private final String redirectUri;
    private final String successRedirectUri;
    private final int connectTimeout;
    private final int readTimeout;

    public GoogleOAuthProperties(
            final String clientId,
            final String clientSecret,
            final String authorizationUri,
            final String tokenUri,
            final String userUri,
            final String redirectUri,
            final String successRedirectUri,
            final int connectTimeout,
            final int readTimeout
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authorizationUri = authorizationUri;
        this.tokenUri = tokenUri;
        this.userUri = userUri;
        this.redirectUri = redirectUri;
        this.successRedirectUri = successRedirectUri;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }
}
