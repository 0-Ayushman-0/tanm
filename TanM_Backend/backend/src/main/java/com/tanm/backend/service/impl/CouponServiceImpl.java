package com.tanm.backend.service.impl;

import com.tanm.backend.dto.CouponCalculationResponse;
import com.tanm.backend.dto.CouponDto;
import com.tanm.backend.entity.*;
import com.tanm.backend.enums.CartStatus;
import com.tanm.backend.enums.CouponType;
import com.tanm.backend.exception.BadRequestException;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.repository.*;
import com.tanm.backend.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository couponRedemptionRepository;
    private final CartRepository cartRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public CouponCalculationResponse calculateDiscount(String code, AppUser user) {
        Coupon coupon = couponRepository.findByCodeIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(code)
                .orElseThrow(() -> new BadRequestException("Coupon code is invalid or inactive."));

        Cart cart = cartRepository.findByUserAndStatusWithItems(user, CartStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("No active shopping cart found."));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty.");
        }

        // Perform standard validations (expiry, limits, per-user limits, minOrder)
        validateCouponBasics(coupon, user, cart);

        // Calculate discount based on item applicability
        BigDecimal discount = computeDiscountForCart(coupon, cart);

        BigDecimal subtotal = cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Grand total is subtotal minus discount. In a full shipping fee context, it would add shipping.
        // We'll calculate the new grand total assuming shipping fee from checkout request, 
        // but here we just show the cart level final subtotal/grand total before checkout taxes/fees.
        BigDecimal finalTotal = subtotal.subtract(discount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
        }

        return CouponCalculationResponse.builder()
                .isValid(true)
                .message("Coupon applied successfully.")
                .code(coupon.getCode())
                .type(coupon.getType())
                .couponValue(coupon.getValue())
                .originalSubtotal(subtotal)
                .discountAmount(discount)
                .finalGrandTotal(finalTotal)
                .build();
    }

    @Override
    @Transactional
    public BigDecimal redeemCoupon(String code, AppUser user, Order order) {
        // 1. Fetch Coupon with Pessimistic Write Lock
        Coupon coupon = couponRepository.findByCodeIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(code)
                .orElseThrow(() -> new BadRequestException("Coupon code is invalid or inactive."));

        coupon = couponRepository.findByIdForUpdate(coupon.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found."));

        Cart cart = cartRepository.findByUserAndStatusWithItems(user, CartStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("No active shopping cart found."));

        // 2. Perform validations again inside locked transaction
        validateCouponBasics(coupon, user, cart);

        // 3. Compute final discount
        BigDecimal discount = computeDiscountForCart(coupon, cart);

        // 4. Increment usage statistics
        coupon.setUsageCount(coupon.getUsageCount() + 1);
        if (coupon.getUsageLimit() != null && coupon.getUsageCount() > coupon.getUsageLimit()) {
            throw new BadRequestException("Coupon limit has been exceeded.");
        }
        couponRepository.save(coupon);

        // 5. Log CouponRedemption
        CouponRedemption redemption = CouponRedemption.builder()
                .coupon(coupon)
                .user(user)
                .order(order)
                .redeemedAt(LocalDateTime.now())
                .build();
        couponRedemptionRepository.save(redemption);

        log.info("Coupon [{}] redeemed successfully for User [{}] Order [{}]", coupon.getCode(), user.getEmail(), order.getOrderNumber());
        return discount;
    }

    // ========================================================
    // Validations & Calculator Helpers
    // ========================================================

    private void validateCouponBasics(Coupon coupon, AppUser user, Cart cart) {
        if (coupon.isExpired()) {
            throw new BadRequestException("Coupon has expired.");
        }

        // Global limits
        if (coupon.getUsageLimit() != null && coupon.getUsageCount() >= coupon.getUsageLimit()) {
            throw new BadRequestException("Coupon usage limit reached.");
        }

        // Per-user redemption limit checks
        long userRedemptions = couponRedemptionRepository.countByUserAndCoupon(user, coupon);
        if (userRedemptions >= coupon.getMaxUsesPerUser()) {
            throw new BadRequestException("You have already used this coupon code.");
        }

        // Min order check
        BigDecimal cartSubtotal = cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (coupon.getMinOrder() != null && cartSubtotal.compareTo(coupon.getMinOrder()) < 0) {
            throw new BadRequestException("Minimum order value of ₹" + coupon.getMinOrder() + " required to use this coupon.");
        }
    }

    private BigDecimal computeDiscountForCart(Coupon coupon, Cart cart) {
        BigDecimal applicableSubtotal = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            Category category = product.getCategory();

            // Check exclusions first
            boolean isExcluded = coupon.getExcludedProducts().contains(product) ||
                    (category != null && coupon.getExcludedCategories().contains(category));

            if (isExcluded) {
                continue;
            }

            // Check inclusions
            boolean isIncluded = true;
            boolean hasInclusions = !coupon.getApplicableProducts().isEmpty() || !coupon.getApplicableCategories().isEmpty();

            if (hasInclusions) {
                isIncluded = coupon.getApplicableProducts().contains(product) ||
                        (category != null && coupon.getApplicableCategories().contains(category));
            }

            if (isIncluded) {
                BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                applicableSubtotal = applicableSubtotal.add(itemSubtotal);
            }
        }

        if (applicableSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;

        if (coupon.getType() == CouponType.PERCENTAGE) {
            discount = applicableSubtotal.multiply(coupon.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
                discount = coupon.getMaxDiscount();
            }
        } else if (coupon.getType() == CouponType.FLAT) {
            discount = coupon.getValue();
            if (discount.compareTo(applicableSubtotal) > 0) {
                discount = applicableSubtotal; // Flat discount cannot exceed applicable items subtotal
            }
        } else if (coupon.getType() == CouponType.FREE_SHIPPING) {
            // Free shipping discount value matches shipping fee (determined during order integration)
            // The discount value itself is resolved during checkout. Returning zero for preview if no shipping is loaded.
            return BigDecimal.ZERO;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    // ========================================================
    // Administrative Operations & Mapping
    // ========================================================

    @Override
    @Transactional
    public CouponDto createCoupon(CouponDto dto) {
        if (couponRepository.findByCodeIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(dto.getCode()).isPresent()) {
            throw new BadRequestException("Active coupon code already exists.");
        }
        Coupon coupon = mapToEntity(dto);
        Coupon saved = couponRepository.save(coupon);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public CouponDto updateCoupon(Long id, CouponDto dto) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));

        if (coupon.isDeleted()) {
            throw new BadRequestException("Cannot update a deleted coupon.");
        }

        coupon.setCode(dto.getCode());
        coupon.setType(dto.getType());
        coupon.setValue(dto.getValue());
        coupon.setMinOrder(dto.getMinOrder());
        coupon.setMaxDiscount(dto.getMaxDiscount());
        coupon.setUsageLimit(dto.getUsageLimit());
        coupon.setMaxUsesPerUser(dto.getMaxUsesPerUser());
        coupon.setExpiresAt(dto.getExpiresAt());
        coupon.setActive(dto.isActive());

        // Update inclusions/exclusions
        if (dto.getApplicableProductIds() != null) {
            coupon.setApplicableProducts(productRepository.findAllById(dto.getApplicableProductIds()).stream().collect(Collectors.toSet()));
        }
        if (dto.getApplicableCategoryIds() != null) {
            coupon.setApplicableCategories(categoryRepository.findAllById(dto.getApplicableCategoryIds()).stream().collect(Collectors.toSet()));
        }
        if (dto.getExcludedProductIds() != null) {
            coupon.setExcludedProducts(productRepository.findAllById(dto.getExcludedProductIds()).stream().collect(Collectors.toSet()));
        }
        if (dto.getExcludedCategoryIds() != null) {
            coupon.setExcludedCategories(categoryRepository.findAllById(dto.getExcludedCategoryIds()).stream().collect(Collectors.toSet()));
        }

        Coupon saved = couponRepository.save(coupon);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponDto getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));
        if (coupon.isDeleted()) {
            throw new ResourceNotFoundException("Coupon has been deleted.");
        }
        return mapToDto(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponDto> getCoupons(Pageable pageable) {
        return couponRepository.findAllByIsDeletedFalse(pageable).map(this::mapToDto);
    }

    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));
        coupon.setDeleted(true);
        couponRepository.save(coupon);
    }

    private Coupon mapToEntity(CouponDto dto) {
        Coupon coupon = Coupon.builder()
                .code(dto.getCode())
                .type(dto.getType())
                .value(dto.getValue())
                .minOrder(dto.getMinOrder())
                .maxDiscount(dto.getMaxDiscount())
                .usageLimit(dto.getUsageLimit())
                .maxUsesPerUser(dto.getMaxUsesPerUser() <= 0 ? 1 : dto.getMaxUsesPerUser())
                .expiresAt(dto.getExpiresAt())
                .isActive(dto.isActive())
                .build();

        if (dto.getApplicableProductIds() != null) {
            coupon.setApplicableProducts(productRepository.findAllById(dto.getApplicableProductIds()).stream().collect(Collectors.toSet()));
        }
        if (dto.getApplicableCategoryIds() != null) {
            coupon.setApplicableCategories(categoryRepository.findAllById(dto.getApplicableCategoryIds()).stream().collect(Collectors.toSet()));
        }
        if (dto.getExcludedProductIds() != null) {
            coupon.setExcludedProducts(productRepository.findAllById(dto.getExcludedProductIds()).stream().collect(Collectors.toSet()));
        }
        if (dto.getExcludedCategoryIds() != null) {
            coupon.setExcludedCategories(categoryRepository.findAllById(dto.getExcludedCategoryIds()).stream().collect(Collectors.toSet()));
        }

        return coupon;
    }

    private CouponDto mapToDto(Coupon coupon) {
        return CouponDto.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .type(coupon.getType())
                .value(coupon.getValue())
                .minOrder(coupon.getMinOrder())
                .maxDiscount(coupon.getMaxDiscount())
                .usageLimit(coupon.getUsageLimit())
                .usageCount(coupon.getUsageCount())
                .maxUsesPerUser(coupon.getMaxUsesPerUser())
                .expiresAt(coupon.getExpiresAt())
                .isActive(coupon.isActive())
                .applicableCategoryIds(coupon.getApplicableCategories().stream().map(Category::getId).collect(Collectors.toSet()))
                .applicableProductIds(coupon.getApplicableProducts().stream().map(Product::getId).collect(Collectors.toSet()))
                .excludedCategoryIds(coupon.getExcludedCategories().stream().map(Category::getId).collect(Collectors.toSet()))
                .excludedProductIds(coupon.getExcludedProducts().stream().map(Product::getId).collect(Collectors.toSet()))
                .build();
    }
}
