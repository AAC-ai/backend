package com.aac.backend.presentation.cookie;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieProvider {

    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    public ResponseCookie createCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/api/auth")
                .build();
    }

    public ResponseCookie clearCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/api/auth")
                .maxAge(0)
                .build();
    }
}
