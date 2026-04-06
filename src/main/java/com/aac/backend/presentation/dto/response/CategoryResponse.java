package com.aac.backend.presentation.dto.response;

import com.aac.backend.domain.Category;

public record CategoryResponse(
        Long id,
        String name,
        String label
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getLabel()
        );
    }
}
