package com.tanm.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDto {
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private BigDecimal yesterdayRevenue;
    private BigDecimal monthRevenue;
    private long totalOrders;
    private long totalCustomers;
    private BigDecimal averageOrderValue;
    private long pendingOrders;
    private long cancelledOrders;
    private long refundedOrders;
    private double conversionRate;
    private double averageItemsPerOrder;
    private long newCustomers;
    private long returningCustomers;
    private long outOfStockProducts;
}
