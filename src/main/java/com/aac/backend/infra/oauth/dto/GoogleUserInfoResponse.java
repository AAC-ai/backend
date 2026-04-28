package com.aac.backend.infra.oauth.dto;

public record GoogleUserInfoResponse(
        String email,
        String name,
        String picture
) {}
