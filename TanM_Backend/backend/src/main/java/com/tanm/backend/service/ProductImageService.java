package com.tanm.backend.service;

import com.tanm.backend.dto.ProductImageAddRequest;
import com.tanm.backend.dto.ProductImageDto;
import com.tanm.backend.dto.ProductImageReorderRequest;

public interface ProductImageService {
    ProductImageDto addProductImage(Long productId, ProductImageAddRequest request);
    java.util.List<ProductImageDto> addProductImagesBulk(Long productId, java.util.List<ProductImageAddRequest> requests);
    void removeProductImage(Long productId, Long imageId);
    void reorderProductImages(Long productId, ProductImageReorderRequest request);
    ProductImageDto setPrimaryImage(Long productId, Long imageId);
}
