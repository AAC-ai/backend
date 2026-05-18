package com.aac.backend.presentation;

import com.aac.backend.presentation.cookie.TokenCookieProvider;
import com.aac.backend.presentation.dto.response.ApiResponse;
import com.aac.backend.service.AuthService;
import com.aac.backend.service.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final TokenCookieProvider cookieProvider;

    @GetMapping("/google")
    public ResponseEntity<Void> redirectToGoogle() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(googleAuthService.getAuthorizationUrl()))
                .build();
    }

    @GetMapping("/google/callback")
    public ResponseEntity<Void> googleCallback(@RequestParam String code) {
        var tokens = googleAuthService.login(code);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(googleAuthService.getSuccessRedirectUri()))
                .header(HttpHeaders.SET_COOKIE,
                        cookieProvider.createAccessTokenCookie(tokens.accessToken()).toString(),
                        cookieProvider.createRefreshTokenCookie(tokens.refreshToken()).toString())
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> reissue(
            @CookieValue(TokenCookieProvider.REFRESH_TOKEN_COOKIE) String refreshToken
    ) {
        var tokens = authService.reissue(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        cookieProvider.createAccessTokenCookie(tokens.accessToken()).toString(),
                        cookieProvider.createRefreshTokenCookie(tokens.refreshToken()).toString())
                .body(ApiResponse.success(null));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE,
                        cookieProvider.clearAccessTokenCookie().toString(),
                        cookieProvider.clearRefreshTokenCookie().toString())
                .build();
    }
}
