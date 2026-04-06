package com.aac.backend.controller.dto.response;

import com.aac.backend.domain.Word;

public record WordResponse(
        Long id,
        String label,
        String imageUrl,
        String category
) {
    public static WordResponse from(Word word) {
        return new WordResponse(
                word.getId(),
                word.getLabel(),
                word.getImageUrl(),
                word.getCategory().getName()
        );
    }
}
