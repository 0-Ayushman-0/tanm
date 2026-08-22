package com.tanm.backend.service;

import com.tanm.backend.dto.ProductCreateRequest;
import com.tanm.backend.dto.ProductDto;

import java.util.List;

public interface ProductService {
    ProductDto createProduct(ProductCreateRequest request);
    ProductDto getProductById(Long id);
    ProductDto getProductBySlug(String slug);
    org.springframework.data.domain.Page<ProductDto> getAllProducts(org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<ProductDto> getProductsByCategory(Long categoryId, org.springframework.data.domain.Pageable pageable);
    ProductDto updateProduct(Long id, ProductCreateRequest request);
    void deleteProduct(Long id);
    org.springframework.data.domain.Page<ProductDto> filterProducts(com.tanm.backend.dto.ProductFilterRequest filter, org.springframework.data.domain.Pageable pageable);
}
