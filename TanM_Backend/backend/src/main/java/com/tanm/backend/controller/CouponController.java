package com.tanm.backend.controller;

import com.tanm.backend.dto.CouponCalculationResponse;
import com.tanm.backend.dto.VerifyCouponRequest;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/validate")
    public ResponseEntity<CouponCalculationResponse> validateCoupon(
            @Valid @RequestBody VerifyCouponRequest request,
            @AuthenticationPrincipal AppUser user
    ) {
        CouponCalculationResponse response = couponService.calculateDiscount(request.getCode(), user);
        return ResponseEntity.ok(response);
    }
}
