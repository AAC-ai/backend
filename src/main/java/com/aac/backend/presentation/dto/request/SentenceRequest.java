package com.aac.backend.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SentenceRequest(
        @NotEmpty List<WordRequest> words
) {}
