package com.tanm.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryAuditDto {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private int previousStock;
    private int newStock;
    private int quantityChanged;
    private String adjustmentType;
    private String reason;
    private String adjustedBy;
    private LocalDateTime createdAt;
}
