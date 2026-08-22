package com.tanm.backend.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalSearchResponseDto {
    private String query;
    private List<ProductDto> products;
    private List<CollectionDto> collections;
    private List<CategoryDto> categories;
    private long totalProducts;
    private long totalCollections;
    private long totalCategories;
}
