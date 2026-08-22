package com.tanm.backend.dto;

import com.tanm.backend.enums.CartStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private Long id;
    private String guestToken;
    private String couponCode;
    private CartStatus status;
    private List<CartItemDto> items;
    private int totalQuantity;
    private BigDecimal totalPrice;

    @com.fasterxml.jackson.annotation.JsonProperty("subtotal")
    public BigDecimal getSubtotal() {
        return totalPrice;
    }
}
