package com.tanm.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopProductDto {
    private Long productId;
    private String productName;
    private Long quantitySold;
    private BigDecimal revenueGenerated;
}
