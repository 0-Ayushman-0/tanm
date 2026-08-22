package com.tanm.backend.mapper;

import com.tanm.backend.dto.ProductCreateRequest;
import com.tanm.backend.dto.ProductDto;
import com.tanm.backend.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final CategoryMapper categoryMapper;

    public ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .shortDescription(product.getShortDescription())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .mainImageUrl(product.getImages() != null ? product.getImages().stream()
                        .filter(img -> !img.isDeleted() && img.isPrimary())
                        .map(com.tanm.backend.entity.ProductImage::getImageUrl)
                        .findFirst()
                        .orElseGet(() -> product.getImages().stream()
                                .filter(img -> !img.isDeleted())
                                .map(com.tanm.backend.entity.ProductImage::getImageUrl)
                                .findFirst()
                                .orElse(null)) : null)
                .leatherType(product.getLeatherType())
                .color(product.getColor())
                .dimensions(product.getDimensions())
                .isFeatured(product.isFeatured())
                .status(product.getStatus())
                .category(categoryMapper.toDto(product.getCategory()))
                .images(product.getImages() != null ? product.getImages().stream()
                        .filter(img -> !img.isDeleted())
                        .sorted((a, b) -> {
                            if (a.isPrimary() != b.isPrimary()) {
                                return a.isPrimary() ? -1 : 1;
                            }
                            return Integer.compare(
                                    a.getDisplayOrder() != null ? a.getDisplayOrder() : 0,
                                    b.getDisplayOrder() != null ? b.getDisplayOrder() : 0
                            );
                        })
                        .map(this::toProductImageDto)
                        .collect(java.util.stream.Collectors.toList()) : null)
                .isActive(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public com.tanm.backend.dto.ProductImageDto toProductImageDto(com.tanm.backend.entity.ProductImage productImage) {
        if (productImage == null) {
            return null;
        }
        return com.tanm.backend.dto.ProductImageDto.builder()
                .id(productImage.getId())
                .imageUrl(productImage.getImageUrl())
                .publicId(productImage.getPublicId())
                .altText(productImage.getAltText())
                .displayOrder(productImage.getDisplayOrder())
                .isPrimary(productImage.isPrimary())
                .build();
    }

    public Product toEntity(ProductCreateRequest request) {
        if (request == null) {
            return null;
        }
        return Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .leatherType(request.getLeatherType())
                .color(request.getColor())
                .dimensions(request.getDimensions())
                .isFeatured(request.isFeatured())
                .status(request.getStatus())
                .build();
    }

    public void updateEntityFromRequest(ProductCreateRequest request, Product product) {
        if (request == null || product == null) {
            return;
        }
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setShortDescription(request.getShortDescription());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setLeatherType(request.getLeatherType());
        product.setColor(request.getColor());
        product.setDimensions(request.getDimensions());
        product.setFeatured(request.isFeatured());
        product.setStatus(request.getStatus());
    }
}
