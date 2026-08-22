package com.tanm.backend.service;

import com.tanm.backend.dto.CouponCalculationResponse;
import com.tanm.backend.dto.CouponDto;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CouponService {

    /**
     * Fetch active cart for user, check applicability rules, exclusions, limits,
     * and calculate the current discount amount without modifying state.
     */
    CouponCalculationResponse calculateDiscount(String code, AppUser user);

    /**
     * Pessimistically lock coupon, confirm validity criteria, and register a redemption audit log.
     */
    BigDecimal redeemCoupon(String code, AppUser user, Order order);

    // Administrative Operations
    CouponDto createCoupon(CouponDto couponDto);

    CouponDto updateCoupon(Long id, CouponDto couponDto);

    CouponDto getCouponById(Long id);

    Page<CouponDto> getCoupons(Pageable pageable);

    void deleteCoupon(Long id);
}
