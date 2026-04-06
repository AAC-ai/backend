package com.aac.backend.presentation;

import com.aac.backend.presentation.dto.response.CategoryResponse;
import com.aac.backend.domain.Category;
import com.aac.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        List<Category> categories = categoryService.getCategories();

        return ResponseEntity.ok(categories.stream()
                .map(CategoryResponse::from)
                .toList());
    }
}
