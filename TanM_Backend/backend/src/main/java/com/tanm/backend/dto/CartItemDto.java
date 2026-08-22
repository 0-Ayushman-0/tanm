package com.tanm.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private Long id;
    private Long productId;
    private String productName;
    private String slug;
    private BigDecimal price;
    private String primaryImageUrl;

    @com.fasterxml.jackson.annotation.JsonProperty("productImageUrl")
    public String getProductImageUrl() {
        return primaryImageUrl;
    }
    private int stockRemaining;
    private int quantity;
    private BigDecimal subtotal;
    private boolean isAvailable;
    private String message;
}
