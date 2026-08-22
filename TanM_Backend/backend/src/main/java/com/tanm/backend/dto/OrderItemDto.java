package com.tanm.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {

    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private String slug;
    private String primaryImageUrl;
    private String color;
    private String leatherType;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
