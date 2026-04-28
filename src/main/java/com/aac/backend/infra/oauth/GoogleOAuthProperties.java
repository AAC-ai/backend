package com.aac.backend.infra.oauth;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.google")
@Getter
public class GoogleOAuthProperties {

    private final String clientId;
    private final String clientSecret;
    private final String tokenUri;
    private final String userUri;
    private final String redirectUri;
    private final int connectTimeout;
    private final int readTimeout;

    public GoogleOAuthProperties(
            final String clientId,
            final String clientSecret,
            final String tokenUri,
            final String userUri,
            final String redirectUri,
            final int connectTimeout,
            final int readTimeout
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenUri = tokenUri;
        this.userUri = userUri;
        this.redirectUri = redirectUri;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }
}
