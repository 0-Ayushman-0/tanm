package com.tanm.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryStockAdjustmentRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "New stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer newStockQuantity;

    @NotBlank(message = "Adjustment type is required")
    private String adjustmentType; // e.g., RESTOCK, DAMAGED, MANUAL_CORRECTION

    private String reason;
}
