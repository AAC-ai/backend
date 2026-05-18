package com.aac.backend.service;

import com.aac.backend.domain.RefreshToken;
import com.aac.backend.domain.RefreshTokenRepository;
import com.aac.backend.global.exception.BusinessException;
import com.aac.backend.global.exception.ErrorCode;
import com.aac.backend.infra.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public record AuthTokens(String accessToken, String refreshToken) {}

    @Transactional
    public AuthTokens reissue(String refreshTokenValue) {
        var refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        refreshTokenRepository.delete(refreshToken);

        var userId = refreshToken.getUserId();
        var newAccessToken = jwtProvider.issue(userId);
        var newRefreshToken = issueAndSaveRefreshToken(userId);

        return new AuthTokens(newAccessToken, newRefreshToken);
    }

    public AuthTokens issueTokens(Long userId) {
        var accessToken = jwtProvider.issue(userId);
        var refreshToken = issueAndSaveRefreshToken(userId);
        return new AuthTokens(accessToken, refreshToken);
    }

    private String issueAndSaveRefreshToken(Long userId) {
        var token = jwtProvider.issueRefreshToken();
        var expiresAt = jwtProvider.refreshExpiresAt();
        refreshTokenRepository.save(RefreshToken.create(token, userId, expiresAt));
        return token;
    }
}
