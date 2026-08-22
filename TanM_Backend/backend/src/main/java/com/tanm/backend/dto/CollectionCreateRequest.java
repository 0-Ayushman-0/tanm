package com.tanm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionCreateRequest {

    @NotBlank(message = "Collection name cannot be blank")
    @Size(max = 100, message = "Collection name must be less than 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @Size(max = 2048, message = "Image URL must be less than 2048 characters")
    private String imageUrl;

    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    private boolean isFeatured = false;
}
