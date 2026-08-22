package com.tanm.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageReorderRequest {

    @NotNull(message = "Image orders list is required")
    private List<ImageOrderPair> imageOrders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageOrderPair {
        @NotNull(message = "Image ID is required")
        private Long imageId;

        @NotNull(message = "Display order is required")
        private Integer displayOrder;
    }
}
