package com.aac.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TtsRequest(
        @NotBlank String text
) {
}
