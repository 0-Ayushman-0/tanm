package com.tanm.backend.service.impl;

import com.tanm.backend.dto.CouponCalculationResponse;
import com.tanm.backend.entity.*;
import com.tanm.backend.enums.CartStatus;
import com.tanm.backend.enums.CouponType;
import com.tanm.backend.exception.BadRequestException;
import com.tanm.backend.repository.CartRepository;
import com.tanm.backend.repository.CouponRedemptionRepository;
import com.tanm.backend.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponRedemptionRepository couponRedemptionRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    private AppUser user;
    private Cart cart;
    private Product p1;
    private Product p2;
    private Category c1;

    @BeforeEach
    void setUp() {
        user = AppUser.builder().email("test@example.com").build();
        c1 = Category.builder().name("Electronics").build();

        p1 = Product.builder()
                .price(BigDecimal.valueOf(100.00))
                .category(c1)
                .build();
        p1.setId(101L);

        p2 = Product.builder()
                .price(BigDecimal.valueOf(200.00))
                .build();
        p2.setId(102L);

        CartItem item1 = CartItem.builder().product(p1).quantity(2).build();
        CartItem item2 = CartItem.builder().product(p2).quantity(1).build();

        cart = Cart.builder()
                .user(user)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>(Arrays.asList(item1, item2)))
                .build();
    }

    @Test
    void calculateDiscount_percentageCoupon_shouldCalculateCorrectly() {
        Coupon coupon = Coupon.builder()
                .code("SAVE10")
                .type(CouponType.PERCENTAGE)
                .value(BigDecimal.valueOf(10)) // 10%
                .maxUsesPerUser(1)
                .usageLimit(100)
                .usageCount(0)
                .expiresAt(LocalDateTime.now().plusDays(10))
                .applicableProducts(new HashSet<>())
                .applicableCategories(new HashSet<>())
                .excludedProducts(new HashSet<>())
                .excludedCategories(new HashSet<>())
                .build();

        Mockito.when(couponRepository.findByCodeIgnoreCaseAndIsActiveTrueAndIsDeletedFalse("SAVE10"))
                .thenReturn(Optional.of(coupon));
        Mockito.when(cartRepository.findByUserAndStatusWithItems(user, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        Mockito.when(couponRedemptionRepository.countByUserAndCoupon(user, coupon))
                .thenReturn(0L);

        CouponCalculationResponse response = couponService.calculateDiscount("SAVE10", user);

        assertThat(response.isValid()).isTrue();
        // Total subtotal: 100*2 + 200*1 = 400. 10% of 400 is 40.0
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(40.00));
    }

    @Test
    void calculateDiscount_flatCoupon_shouldCalculateCorrectly() {
        Coupon coupon = Coupon.builder()
                .code("FLAT50")
                .type(CouponType.FLAT)
                .value(BigDecimal.valueOf(50.00))
                .maxUsesPerUser(2)
                .usageLimit(50)
                .usageCount(5)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .applicableProducts(new HashSet<>())
                .applicableCategories(new HashSet<>())
                .excludedProducts(new HashSet<>())
                .excludedCategories(new HashSet<>())
                .build();

        Mockito.when(couponRepository.findByCodeIgnoreCaseAndIsActiveTrueAndIsDeletedFalse("FLAT50"))
                .thenReturn(Optional.of(coupon));
        Mockito.when(cartRepository.findByUserAndStatusWithItems(user, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        CouponCalculationResponse response = couponService.calculateDiscount("FLAT50", user);

        assertThat(response.isValid()).isTrue();
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
    }

    @Test
    void calculateDiscount_expiredCoupon_shouldThrowException() {
        Coupon coupon = Coupon.builder()
                .code("EXPIRED")
                .type(CouponType.FLAT)
                .value(BigDecimal.valueOf(10))
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        Mockito.when(couponRepository.findByCodeIgnoreCaseAndIsActiveTrueAndIsDeletedFalse("EXPIRED"))
                .thenReturn(Optional.of(coupon));
        Mockito.when(cartRepository.findByUserAndStatusWithItems(user, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> couponService.calculateDiscount("EXPIRED", user))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void calculateDiscount_exclusionOverridesInclusion_shouldExcludeDiscount() {
        // Coupon applicable to category Electronics, but explicitly excludes product p1
        Coupon coupon = Coupon.builder()
                .code("ELECTRO")
                .type(CouponType.PERCENTAGE)
                .value(BigDecimal.valueOf(20)) // 20%
                .maxUsesPerUser(1)
                .usageLimit(10)
                .usageCount(0)
                .expiresAt(LocalDateTime.now().plusDays(5))
                .applicableCategories(new HashSet<>(Collections.singletonList(c1)))
                .applicableProducts(new HashSet<>())
                .excludedProducts(new HashSet<>(Collections.singletonList(p1))) // overrides inclusion!
                .excludedCategories(new HashSet<>())
                .build();

        Mockito.when(couponRepository.findByCodeIgnoreCaseAndIsActiveTrueAndIsDeletedFalse("ELECTRO"))
                .thenReturn(Optional.of(coupon));
        Mockito.when(cartRepository.findByUserAndStatusWithItems(user, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        CouponCalculationResponse response = couponService.calculateDiscount("ELECTRO", user);

        assertThat(response.isValid()).isTrue();
        // Total subtotal: p1 (100*2 = 200, category c1 but excluded product) + p2 (200*1 = 200, category null/not in category inclusion)
        // Hence applicable total = 0. Discount amount = 0
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
