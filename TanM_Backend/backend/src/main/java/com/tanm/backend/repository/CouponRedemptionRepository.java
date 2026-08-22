package com.tanm.backend.repository;

import com.tanm.backend.entity.AppUser;
import com.tanm.backend.entity.Coupon;
import com.tanm.backend.entity.CouponRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, Long> {

    @Query("SELECT COUNT(cr) FROM CouponRedemption cr WHERE cr.user = :user AND cr.coupon = :coupon")
    long countByUserAndCoupon(@Param("user") AppUser user, @Param("coupon") Coupon coupon);
}
