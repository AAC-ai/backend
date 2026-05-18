package com.aac.backend.infra.oauth;

import com.aac.backend.global.exception.BusinessException;
import com.aac.backend.global.exception.ErrorCode;
import com.aac.backend.infra.oauth.dto.GoogleAccessTokenResponse;
import com.aac.backend.infra.oauth.dto.GoogleUserInfoResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Component
@EnableConfigurationProperties(GoogleOAuthProperties.class)
public class GoogleOAuthClient {

    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

    private final GoogleOAuthProperties properties;
    private final RestClient restClient;

    public GoogleOAuthClient(
            final GoogleOAuthProperties properties,
            final RestClient.Builder restClientBuilder
    ) {
        this.properties = properties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());
        this.restClient = restClientBuilder.requestFactory(factory).build();
    }

    public GoogleUserInfoResponse getUserInfo(final String code) {
        String accessToken = requestAccessToken(code);
        return requestUserInfo(accessToken);
    }

    private String requestAccessToken(final String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", properties.getClientId());
        params.add("client_secret", properties.getClientSecret());
        params.add("redirect_uri", properties.getRedirectUri());
        params.add("grant_type", GRANT_TYPE_AUTHORIZATION_CODE);

        try {
            GoogleAccessTokenResponse response = restClient.post()
                    .uri(properties.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(GoogleAccessTokenResponse.class);
            return Objects.requireNonNull(response).accessToken();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private GoogleUserInfoResponse requestUserInfo(final String accessToken) {
        return restClient.get()
                .uri(properties.getUserUri())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(GoogleUserInfoResponse.class);
    }
}
