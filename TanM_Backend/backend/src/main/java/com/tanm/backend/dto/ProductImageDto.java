package com.tanm.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDto {
    private Long id;
    private String imageUrl;
    private String publicId;
    private String altText;
    private Integer displayOrder;
    @com.fasterxml.jackson.annotation.JsonProperty("isPrimary")
    private boolean isPrimary;
}
