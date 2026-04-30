package com.aac.backend.service;

import com.aac.backend.domain.User;
import com.aac.backend.domain.UserRepository;
import com.aac.backend.infra.oauth.GoogleOAuthClient;
import com.aac.backend.presentation.dto.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final GoogleOAuthClient googleOAuthClient;
    private final UserRepository userRepository;
    private final AuthService authService;

    @Transactional
    public TokenResponse login(String code) {
        var userInfo = googleOAuthClient.getUserInfo(code);
        var user = userRepository.findByEmail(userInfo.email())
                .orElseGet(() -> userRepository.save(
                        User.create(userInfo.email(), userInfo.name())
                ));
        return authService.issueTokens(user.getId());
    }
}
