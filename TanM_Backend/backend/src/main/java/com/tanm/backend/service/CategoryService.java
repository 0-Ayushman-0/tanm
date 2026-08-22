package com.tanm.backend.service;

import com.tanm.backend.dto.CategoryCreateRequest;
import com.tanm.backend.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(CategoryCreateRequest request);
    CategoryDto getCategoryById(Long id);
    CategoryDto getCategoryBySlug(String slug);
    org.springframework.data.domain.Page<CategoryDto> getAllCategories(org.springframework.data.domain.Pageable pageable);
    CategoryDto updateCategory(Long id, CategoryCreateRequest request);
    void deleteCategory(Long id);
}
