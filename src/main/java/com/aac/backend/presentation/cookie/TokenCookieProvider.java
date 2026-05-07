package com.aac.backend.presentation.cookie;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class TokenCookieProvider {

    public static final String ACCESS_TOKEN_COOKIE = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    public ResponseCookie createAccessTokenCookie(String accessToken) {
        return createCookie(ACCESS_TOKEN_COOKIE, accessToken, "/api");
    }

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return createCookie(REFRESH_TOKEN_COOKIE, refreshToken, "/api/auth");
    }

    public ResponseCookie clearAccessTokenCookie() {
        return clearCookie(ACCESS_TOKEN_COOKIE, "/api");
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return clearCookie(REFRESH_TOKEN_COOKIE, "/api/auth");
    }

    private ResponseCookie createCookie(String name, String value, String path) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path(path)
                .build();
    }

    private ResponseCookie clearCookie(String name, String path) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path(path)
                .maxAge(0)
                .build();
    }
}
