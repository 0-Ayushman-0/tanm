package com.tanm.backend.service.impl;

import com.tanm.backend.dto.CategoryCreateRequest;
import com.tanm.backend.dto.CategoryDto;
import com.tanm.backend.entity.Category;
import com.tanm.backend.exception.BadRequestException;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.mapper.CategoryMapper;
import com.tanm.backend.repository.CategoryRepository;
import com.tanm.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final com.tanm.backend.repository.ProductRepository productRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
        }

        String slug = request.getName().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        if (categoryRepository.existsBySlug(slug)) {
            throw new BadRequestException("Category with name/slug '" + request.getName() + "' already exists");
        }

        Category category = categoryMapper.toEntity(request);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<CategoryDto> getAllCategories(org.springframework.data.domain.Pageable pageable) {
        return categoryRepository.findByIsDeletedFalse(pageable)
                .map(categoryMapper::toDto);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long id, CategoryCreateRequest request) {
        Category category = categoryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        Optional<Category> existingByName = categoryRepository.findByName(request.getName());
        if (existingByName.isPresent() && !existingByName.get().getId().equals(id)) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
        }

        String slug = request.getName().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        Optional<Category> existingBySlug = categoryRepository.findBySlugAndIsDeletedFalse(slug);
        if (existingBySlug.isPresent() && !existingBySlug.get().getId().equals(id)) {
            throw new BadRequestException("Category with name/slug '" + request.getName() + "' already exists");
        }

        categoryMapper.updateEntityFromRequest(request, category);
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (productRepository.existsByCategoryIdAndIsDeletedFalse(id)) {
            throw new BadRequestException("Cannot delete category because it contains active products.");
        }

        category.setDeleted(true);
        categoryRepository.save(category);
    }
}
