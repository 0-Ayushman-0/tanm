package com.tanm.backend.dto;

import com.tanm.backend.enums.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Product name must be less than 150 characters")
    private String name;

    @NotBlank(message = "Product SKU is required")
    @Size(max = 50, message = "SKU must be less than 50 characters")
    private String sku;

    @Size(max = 255, message = "Short description must be less than 255 characters")
    private String shortDescription;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Builder.Default
    private Integer stockQuantity = 0;

    @Size(max = 100, message = "Leather type must be less than 100 characters")
    private String leatherType;

    @Size(max = 50, message = "Color must be less than 50 characters")
    private String color;

    @Size(max = 100, message = "Dimensions must be less than 100 characters")
    private String dimensions;

    @com.fasterxml.jackson.annotation.JsonProperty("isFeatured")
    @Builder.Default
    private boolean isFeatured = false;

    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
