package com.tanm.backend.dto;

import com.tanm.backend.enums.CouponType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponDto {
    private Long id;
    private String code;
    private CouponType type;
    private BigDecimal value;
    private BigDecimal minOrder;
    private BigDecimal maxDiscount;
    private Integer usageLimit;
    private int usageCount;
    private int maxUsesPerUser;
    private LocalDateTime expiresAt;
    private boolean isActive;

    private Set<Long> applicableCategoryIds;
    private Set<Long> applicableProductIds;
    private Set<Long> excludedCategoryIds;
    private Set<Long> excludedProductIds;
}
