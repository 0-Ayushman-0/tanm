package com.tanm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageAddRequest {

    @NotBlank(message = "Image URL is required")
    @jakarta.validation.constraints.Size(max = 2048, message = "Image URL must be less than 2048 characters")
    private String imageUrl;

    @jakarta.validation.constraints.Size(max = 255, message = "Public ID must be less than 255 characters")
    private String publicId;

    private String altText;

    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonProperty("isPrimary")
    private boolean isPrimary = false;
}
