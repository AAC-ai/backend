package com.aac.backend.presentation;

import com.aac.backend.presentation.cookie.RefreshTokenCookieProvider;
import com.aac.backend.presentation.dto.response.ApiResponse;
import com.aac.backend.presentation.dto.response.TokenResponse;
import com.aac.backend.service.AuthService;
import com.aac.backend.service.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final RefreshTokenCookieProvider cookieProvider;

    @GetMapping("/google/callback")
    public ResponseEntity<ApiResponse<TokenResponse>> googleCallback(@RequestParam String code) {
        var tokens = googleAuthService.login(code);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieProvider.createCookie(tokens.refreshToken()).toString())
                .body(ApiResponse.success(new TokenResponse(tokens.accessToken())));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(
            @CookieValue(RefreshTokenCookieProvider.REFRESH_TOKEN_COOKIE) String refreshToken
    ) {
        var tokens = authService.reissue(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieProvider.createCookie(tokens.refreshToken()).toString())
                .body(ApiResponse.success(new TokenResponse(tokens.accessToken())));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieProvider.clearCookie().toString())
                .build();
    }
}
