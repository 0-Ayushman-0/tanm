package com.tanm.backend.service.impl;

import com.tanm.backend.dto.ProductImageAddRequest;
import com.tanm.backend.dto.ProductImageDto;
import com.tanm.backend.dto.ProductImageReorderRequest;
import com.tanm.backend.entity.Product;
import com.tanm.backend.entity.ProductImage;
import com.tanm.backend.exception.BadRequestException;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.mapper.ProductMapper;
import com.tanm.backend.repository.ProductImageRepository;
import com.tanm.backend.repository.ProductRepository;
import com.tanm.backend.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductImageDto addProductImage(Long productId, ProductImageAddRequest request) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // 1. Maximum image count check
        long activeCount = productImageRepository.countByProductIdAndIsDeletedFalse(productId);
        if (activeCount >= 10) {
            throw new BadRequestException("Maximum of 10 images reached for this product");
        }

        // 2. Notion-style Display Order logic
        int displayOrder = request.getDisplayOrder() != null && request.getDisplayOrder() != 0
                ? request.getDisplayOrder()
                : (int) (activeCount + 1) * 10;

        // 3. Primary image determination: if first image, force primary
        boolean isPrimary = request.isPrimary();
        if (activeCount == 0) {
            isPrimary = true;
        }

        if (isPrimary) {
            productImageRepository.resetPrimaryImageForProduct(productId);
        }

        ProductImage image = ProductImage.builder()
                .imageUrl(request.getImageUrl())
                .publicId(request.getPublicId())
                .altText(request.getAltText())
                .displayOrder(displayOrder)
                .isPrimary(isPrimary)
                .product(product)
                .build();

        ProductImage saved = productImageRepository.save(image);
        return productMapper.toProductImageDto(saved);
    }

    @Override
    @Transactional
    public List<ProductImageDto> addProductImagesBulk(Long productId, List<ProductImageAddRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        return requests.stream()
                .map(req -> addProductImage(productId, req))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public void removeProductImage(Long productId, Long imageId) {
        productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        ProductImage image = productImageRepository.findByIdAndIsDeletedFalse(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found with id: " + imageId));

        if (!image.getProduct().getId().equals(productId)) {
            throw new BadRequestException("Product image does not belong to this product");
        }

        boolean wasPrimary = image.isPrimary();
        image.setDeleted(true);
        image.setPrimary(false);
        productImageRepository.save(image);

        if (wasPrimary) {
            List<ProductImage> remaining = productImageRepository.findAllByProductIdAndIsDeletedFalseOrderByDisplayOrderAscIdAsc(productId);
            if (!remaining.isEmpty()) {
                ProductImage nextPrimary = remaining.get(0);
                nextPrimary.setPrimary(true);
                productImageRepository.save(nextPrimary);
            }
        }
    }

    @Override
    @Transactional
    public void reorderProductImages(Long productId, ProductImageReorderRequest request) {
        productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        for (ProductImageReorderRequest.ImageOrderPair pair : request.getImageOrders()) {
            ProductImage image = productImageRepository.findByIdAndIsDeletedFalse(pair.getImageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product image not found with id: " + pair.getImageId()));

            if (!image.getProduct().getId().equals(productId)) {
                throw new BadRequestException("Product image with id " + pair.getImageId() + " does not belong to this product");
            }

            image.setDisplayOrder(pair.getDisplayOrder());
            productImageRepository.save(image);
        }
    }

    @Override
    @Transactional
    public ProductImageDto setPrimaryImage(Long productId, Long imageId) {
        productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        ProductImage image = productImageRepository.findByIdAndIsDeletedFalse(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found with id: " + imageId));

        if (!image.getProduct().getId().equals(productId)) {
            throw new BadRequestException("Product image does not belong to this product");
        }

        productImageRepository.resetPrimaryImageForProduct(productId);
        image.setPrimary(true);
        ProductImage updated = productImageRepository.save(image);

        return productMapper.toProductImageDto(updated);
    }
}
