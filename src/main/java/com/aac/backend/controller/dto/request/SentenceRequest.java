package com.aac.backend.controller.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SentenceRequest(
        @NotEmpty List<Long> wordIds
) {}
