package com.tanm.backend.dto;

import com.tanm.backend.enums.CouponType;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponCalculationResponse {
    private boolean isValid;
    private String message;
    private String code;
    private CouponType type;
    private BigDecimal couponValue;
    private BigDecimal originalSubtotal;
    private BigDecimal discountAmount;
    private BigDecimal finalGrandTotal;
}
