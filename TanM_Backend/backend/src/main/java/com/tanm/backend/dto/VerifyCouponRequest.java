package com.tanm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyCouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String code;
}
