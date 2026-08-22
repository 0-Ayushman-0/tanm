package com.tanm.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFilterRequest {
    private Long categoryId;
    private Long collectionId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String leatherType;
    private String color;
    private Boolean inStockOnly;
    @com.fasterxml.jackson.annotation.JsonProperty("isFeatured")
    private Boolean isFeatured;
    private String sortBy; // price_asc, price_desc, newest, rating_desc
}
