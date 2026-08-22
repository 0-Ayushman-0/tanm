package com.tanm.backend.service.impl;

import com.tanm.backend.dto.CategoryDto;
import com.tanm.backend.dto.CollectionDto;
import com.tanm.backend.dto.GlobalSearchResponseDto;
import com.tanm.backend.dto.ProductDto;
import com.tanm.backend.mapper.CategoryMapper;
import com.tanm.backend.mapper.CollectionMapper;
import com.tanm.backend.mapper.ProductMapper;
import com.tanm.backend.repository.CategoryRepository;
import com.tanm.backend.repository.CollectionRepository;
import com.tanm.backend.repository.ProductRepository;
import com.tanm.backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ProductRepository productRepository;
    private final CollectionRepository collectionRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final CollectionMapper collectionMapper;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public GlobalSearchResponseDto globalSearch(String query, int limit) {
        if (query == null || query.trim().isBlank()) {
            return GlobalSearchResponseDto.builder()
                    .query(query)
                    .products(Collections.emptyList())
                    .collections(Collections.emptyList())
                    .categories(Collections.emptyList())
                    .totalProducts(0)
                    .totalCollections(0)
                    .totalCategories(0)
                    .build();
        }

        String sanitized = query.trim();
        Pageable pageable = PageRequest.of(0, limit > 0 ? limit : 5);

        List<ProductDto> products = productRepository.searchProductsQuick(sanitized, pageable).stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());

        List<CollectionDto> collections = collectionRepository.searchCollectionsQuick(sanitized, pageable).stream()
                .map(collectionMapper::toDto)
                .collect(Collectors.toList());

        List<CategoryDto> categories = categoryRepository.searchCategoriesQuick(sanitized, pageable).stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());

        return GlobalSearchResponseDto.builder()
                .query(sanitized)
                .products(products)
                .collections(collections)
                .categories(categories)
                .totalProducts(products.size())
                .totalCollections(collections.size())
                .totalCategories(categories.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> searchProducts(String query, Pageable pageable) {
        if (query == null || query.trim().isBlank()) {
            return Page.empty(pageable);
        }
        return productRepository.searchProductsPaginated(query.trim(), pageable)
                .map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollectionDto> searchCollections(String query, Pageable pageable) {
        if (query == null || query.trim().isBlank()) {
            return Page.empty(pageable);
        }
        return collectionRepository.searchCollectionsPaginated(query.trim(), pageable)
                .map(collectionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDto> searchCategories(String query, Pageable pageable) {
        if (query == null || query.trim().isBlank()) {
            return Page.empty(pageable);
        }
        return categoryRepository.searchCategoriesPaginated(query.trim(), pageable)
                .map(categoryMapper::toDto);
    }
}
