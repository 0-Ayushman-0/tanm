package com.tanm.backend.service;

import com.tanm.backend.dto.CategoryDto;
import com.tanm.backend.dto.CollectionDto;
import com.tanm.backend.dto.GlobalSearchResponseDto;
import com.tanm.backend.dto.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchService {
    GlobalSearchResponseDto globalSearch(String query, int limit);
    Page<ProductDto> searchProducts(String query, Pageable pageable);
    Page<CollectionDto> searchCollections(String query, Pageable pageable);
    Page<CategoryDto> searchCategories(String query, Pageable pageable);
}
