package com.tanm.backend.service.impl;

import com.tanm.backend.dto.DashboardSummaryDto;
import com.tanm.backend.dto.ProductDto;
import com.tanm.backend.dto.TopProductDto;
import com.tanm.backend.entity.Product;
import com.tanm.backend.enums.FulfillmentStatus;
import com.tanm.backend.enums.PaymentStatus;
import com.tanm.backend.mapper.ProductMapper;
import com.tanm.backend.service.AdminDashboardService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @PersistenceContext
    private final EntityManager entityManager;

    private final ProductMapper productMapper;

    @Value("${app.inventory.low-stock-threshold:5}")
    private int globalLowStockThreshold;

    @Override
    @Cacheable(value = "dashboardSummary")
    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummary() {
        log.info("📊 Computing Dashboard Summary (Cache Miss)...");

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfYesterday = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime startOfThisMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // 1. Revenues
        BigDecimal totalRevenue = entityManager.createQuery(
                "SELECT COALESCE(SUM(o.grandTotal), 0.0) FROM Order o WHERE o.paymentStatus = :paid", BigDecimal.class)
                .setParameter("paid", PaymentStatus.PAID)
                .getSingleResult();

        BigDecimal todayRevenue = entityManager.createQuery(
                "SELECT COALESCE(SUM(o.grandTotal), 0.0) FROM Order o WHERE o.paymentStatus = :paid AND o.orderedAt >= :since", BigDecimal.class)
                .setParameter("paid", PaymentStatus.PAID)
                .setParameter("since", startOfToday)
                .getSingleResult();

        BigDecimal yesterdayRevenue = entityManager.createQuery(
                "SELECT COALESCE(SUM(o.grandTotal), 0.0) FROM Order o WHERE o.paymentStatus = :paid AND o.orderedAt >= :start AND o.orderedAt < :end", BigDecimal.class)
                .setParameter("paid", PaymentStatus.PAID)
                .setParameter("start", startOfYesterday)
                .setParameter("end", startOfToday)
                .getSingleResult();

        BigDecimal monthRevenue = entityManager.createQuery(
                "SELECT COALESCE(SUM(o.grandTotal), 0.0) FROM Order o WHERE o.paymentStatus = :paid AND o.orderedAt >= :since", BigDecimal.class)
                .setParameter("paid", PaymentStatus.PAID)
                .setParameter("since", startOfThisMonth)
                .getSingleResult();

        // 2. Orders volumes
        long totalOrders = entityManager.createQuery("SELECT COUNT(o) FROM Order o", Long.class).getSingleResult();
        long pendingOrders = entityManager.createQuery("SELECT COUNT(o) FROM Order o WHERE o.paymentStatus = :pending", Long.class)
                .setParameter("pending", PaymentStatus.PENDING)
                .getSingleResult();
        long cancelledOrders = entityManager.createQuery("SELECT COUNT(o) FROM Order o WHERE o.fulfillmentStatus = :cancelled", Long.class)
                .setParameter("cancelled", FulfillmentStatus.CANCELLED)
                .getSingleResult();
        long refundedOrders = entityManager.createQuery("SELECT COUNT(o) FROM Order o WHERE o.paymentStatus = :refunded", Long.class)
                .setParameter("refunded", PaymentStatus.REFUNDED)
                .getSingleResult();

        // 3. Customers
        long totalCustomers = entityManager.createQuery(
                "SELECT COUNT(u) FROM AppUser u WHERE u.role = com.tanm.backend.enums.UserRole.CUSTOMER AND u.isDeleted = false AND u.isEmailVerified = true", Long.class)
                .getSingleResult();

        long newCustomers = entityManager.createQuery(
                "SELECT COUNT(u) FROM AppUser u WHERE u.role = com.tanm.backend.enums.UserRole.CUSTOMER AND u.isDeleted = false AND u.createdAt >= :since", Long.class)
                .setParameter("since", thirtyDaysAgo)
                .getSingleResult();

        // Count of users who have placed > 1 paid order
        long returningCustomers = 0;
        List<Long> returningCustomerList = entityManager.createQuery(
                "SELECT COUNT(o) FROM Order o WHERE o.paymentStatus = :paid GROUP BY o.user HAVING COUNT(o) > 1", Long.class)
                .setParameter("paid", PaymentStatus.PAID)
                .getResultList();
        returningCustomers = returningCustomerList.size();

        // 4. Products
        long outOfStockProducts = entityManager.createQuery(
                "SELECT COUNT(p) FROM Product p WHERE p.stockQuantity = 0 AND p.isDeleted = false", Long.class)
                .getSingleResult();

        // 5. Advanced ratios
        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (totalOrders > 0) {
            averageOrderValue = totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        }

        double conversionRate = 0.0;
        if (totalCustomers > 0) {
            conversionRate = ((double) totalOrders / totalCustomers) * 100.0;
        }

        double averageItemsPerOrder = entityManager.createQuery(
                "SELECT COALESCE(AVG(SIZE(o.items)), 0.0) FROM Order o", Double.class)
                .getSingleResult();

        return DashboardSummaryDto.builder()
                .totalRevenue(totalRevenue)
                .todayRevenue(todayRevenue)
                .yesterdayRevenue(yesterdayRevenue)
                .monthRevenue(monthRevenue)
                .totalOrders(totalOrders)
                .totalCustomers(totalCustomers)
                .averageOrderValue(averageOrderValue)
                .pendingOrders(pendingOrders)
                .cancelledOrders(cancelledOrders)
                .refundedOrders(refundedOrders)
                .conversionRate(conversionRate)
                .averageItemsPerOrder(averageItemsPerOrder)
                .newCustomers(newCustomers)
                .returningCustomers(returningCustomers)
                .outOfStockProducts(outOfStockProducts)
                .build();
    }

    @Override
    @Cacheable(value = "dashboardRevenue")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRevenueTrends() {
        log.info("📊 Fetching Revenue Trends (Cache Miss)...");
        // Simple monthly aggregation for current year
        int currentYear = LocalDate.now().getYear();
        List<Object[]> results = entityManager.createQuery(
                "SELECT EXTRACT(MONTH FROM o.orderedAt), SUM(o.grandTotal) " +
                "FROM Order o " +
                "WHERE o.paymentStatus = :paid AND EXTRACT(YEAR FROM o.orderedAt) = :year " +
                "GROUP BY EXTRACT(MONTH FROM o.orderedAt) " +
                "ORDER BY EXTRACT(MONTH FROM o.orderedAt)", Object[].class)
                .setParameter("paid", PaymentStatus.PAID)
                .setParameter("year", currentYear)
                .getResultList();

        List<Map<String, Object>> trends = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("month", row[0]);
            map.put("revenue", row[1]);
            trends.add(map);
        }
        return trends;
    }

    @Override
    @Cacheable(value = "dashboardOrders")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOrderTrends() {
        log.info("📊 Fetching Order Trends (Cache Miss)...");
        int currentYear = LocalDate.now().getYear();
        List<Object[]> results = entityManager.createQuery(
                "SELECT EXTRACT(MONTH FROM o.orderedAt), COUNT(o) " +
                "FROM Order o " +
                "WHERE EXTRACT(YEAR FROM o.orderedAt) = :year " +
                "GROUP BY EXTRACT(MONTH FROM o.orderedAt) " +
                "ORDER BY EXTRACT(MONTH FROM o.orderedAt)", Object[].class)
                .setParameter("year", currentYear)
                .getResultList();

        List<Map<String, Object>> trends = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("month", row[0]);
            map.put("orderCount", row[1]);
            trends.add(map);
        }
        return trends;
    }

    @Override
    @Cacheable(value = "dashboardProducts")
    @Transactional(readOnly = true)
    public List<TopProductDto> getTopProducts(int limit) {
        log.info("📊 Fetching Top Products (Cache Miss)...");
        // Aggregate from OrderItem snapshots to preserve history even if product soft-deleted
        List<Object[]> results = entityManager.createQuery(
                "SELECT oi.product.id, oi.productName, SUM(oi.quantity), SUM(oi.subtotal) " +
                "FROM OrderItem oi " +
                "WHERE oi.order.paymentStatus = :paid " +
                "GROUP BY oi.product.id, oi.productName " +
                "ORDER BY SUM(oi.quantity) DESC", Object[].class)
                .setParameter("paid", PaymentStatus.PAID)
                .setMaxResults(limit)
                .getResultList();

        List<TopProductDto> topProducts = new ArrayList<>();
        for (Object[] row : results) {
            topProducts.add(TopProductDto.builder()
                    .productId((Long) row[0])
                    .productName((String) row[1])
                    .quantitySold((Long) row[2])
                    .revenueGenerated((BigDecimal) row[3])
                    .build());
        }
        return topProducts;
    }

    @Override
    @Cacheable(value = "dashboardInventory")
    @Transactional(readOnly = true)
    public List<ProductDto> getInventoryAlerts() {
        log.info("📊 Fetching Inventory Alerts (Cache Miss)...");
        // Products with stock <= threshold or stock <= global low stock threshold if null
        List<Product> products = entityManager.createQuery(
                "SELECT p FROM Product p " +
                "WHERE p.isDeleted = false AND p.stockQuantity <= COALESCE(p.lowStockThreshold, :globalLowThreshold)", Product.class)
                .setParameter("globalLowThreshold", globalLowStockThreshold)
                .getResultList();

        return products.stream()
                .map(productMapper::toDto)
                .toList();
    }
}
