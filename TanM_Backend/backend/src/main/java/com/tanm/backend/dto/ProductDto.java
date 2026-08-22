package com.tanm.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tanm.backend.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String slug;
    private String sku;
    private String shortDescription;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String mainImageUrl;
    private String leatherType;
    private String color;
    private String dimensions;
    @JsonProperty("isFeatured")
    private boolean isFeatured;
    private ProductStatus status;
    private CategoryDto category;
    private java.util.List<ProductImageDto> images;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
