package com.aac.backend.presentation.dto.response;

public record ApiResponse<T>(int status, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }
}
