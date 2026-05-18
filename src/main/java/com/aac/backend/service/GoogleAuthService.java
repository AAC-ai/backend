package com.aac.backend.service;

import com.aac.backend.domain.User;
import com.aac.backend.domain.UserRepository;
import com.aac.backend.infra.oauth.GoogleOAuthClient;
import com.aac.backend.infra.oauth.GoogleOAuthProperties;
import com.aac.backend.service.AuthService.AuthTokens;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final GoogleOAuthClient googleOAuthClient;
    private final GoogleOAuthProperties googleOAuthProperties;
    private final UserRepository userRepository;
    private final AuthService authService;

    public String getAuthorizationUrl() {
        return UriComponentsBuilder.fromUriString(googleOAuthProperties.getAuthorizationUri())
                .queryParam("client_id", googleOAuthProperties.getClientId())
                .queryParam("redirect_uri", googleOAuthProperties.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .build()
                .encode()
                .toUriString();
    }

    public String getSuccessRedirectUri() {
        return googleOAuthProperties.getSuccessRedirectUri();
    }

    @Transactional
    public AuthTokens login(String code) {
        var userInfo = googleOAuthClient.getUserInfo(code);
        var user = userRepository.findByEmail(userInfo.email())
                .orElseGet(() -> userRepository.save(
                        User.create(userInfo.email(), userInfo.name())
                ));
        return authService.issueTokens(user.getId());
    }
}
