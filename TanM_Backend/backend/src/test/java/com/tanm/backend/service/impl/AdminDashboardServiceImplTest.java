package com.tanm.backend.service.impl;

import com.tanm.backend.dto.DashboardSummaryDto;
import com.tanm.backend.dto.ProductDto;
import com.tanm.backend.dto.TopProductDto;
import com.tanm.backend.entity.Product;
import com.tanm.backend.enums.FulfillmentStatus;
import com.tanm.backend.enums.PaymentStatus;
import com.tanm.backend.mapper.ProductMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private AdminDashboardServiceImpl dashboardService;

    @Mock
    private TypedQuery<BigDecimal> bigDecimalQuery;

    @Mock
    private TypedQuery<Long> longQuery;

    @Mock
    private TypedQuery<Double> doubleQuery;

    @Mock
    private TypedQuery<Object[]> objectArrayQuery;

    @Mock
    private TypedQuery<Product> productQuery;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getSummary_shouldCalculateAggregatesCorrectly() {
        // Mock decimal queries
        Mockito.when(entityManager.createQuery(contains("SELECT COALESCE(SUM(o.grandTotal)"), eq(BigDecimal.class)))
                .thenReturn(bigDecimalQuery);
        Mockito.when(bigDecimalQuery.setParameter(anyString(), any()))
                .thenReturn(bigDecimalQuery);
        Mockito.when(bigDecimalQuery.getSingleResult())
                .thenReturn(BigDecimal.valueOf(1000.00), BigDecimal.valueOf(100.00), BigDecimal.valueOf(50.00), BigDecimal.valueOf(900.00));

        // Mock long queries
        Mockito.when(entityManager.createQuery(contains("SELECT COUNT("), eq(Long.class)))
                .thenReturn(longQuery);
        Mockito.when(longQuery.setParameter(anyString(), any()))
                .thenReturn(longQuery);
        Mockito.when(longQuery.getSingleResult())
                .thenReturn(20L, 5L, 2L, 1L, 50L, 10L, 2L); // totalOrders, pending, cancelled, refunded, customers, newCustomers, outOfStock

        // Mock list query for returning customers group by count
        Mockito.when(longQuery.getResultList())
                .thenReturn(List.of(3L, 2L)); // 2 users with >1 orders

        // Mock double query
        Mockito.when(entityManager.createQuery(contains("AVG(SIZE("), eq(Double.class)))
                .thenReturn(doubleQuery);
        Mockito.when(doubleQuery.getSingleResult())
                .thenReturn(3.5);

        DashboardSummaryDto summary = dashboardService.getSummary();

        assertThat(summary.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(summary.getTodayRevenue()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(summary.getTotalOrders()).isEqualTo(20);
        assertThat(summary.getReturningCustomers()).isEqualTo(2);
        assertThat(summary.getAverageItemsPerOrder()).isEqualTo(3.5);
    }

    @Test
    void getTopProducts_shouldAggregateVolumeAndRevenue() {
        Mockito.when(entityManager.createQuery(contains("oi.product.id, oi.productName, SUM(oi.quantity)"), eq(Object[].class)))
                .thenReturn(objectArrayQuery);
        Mockito.when(objectArrayQuery.setParameter(anyString(), any()))
                .thenReturn(objectArrayQuery);
        Mockito.when(objectArrayQuery.setMaxResults(anyInt()))
                .thenReturn(objectArrayQuery);

        Object[] row1 = new Object[]{101L, "Premium Leather Bag", 15L, BigDecimal.valueOf(15000.00)};
        Mockito.when(objectArrayQuery.getResultList())
                .thenReturn(Collections.singletonList(row1));

        List<TopProductDto> top = dashboardService.getTopProducts(5);

        assertThat(top).hasSize(1);
        assertThat(top.get(0).getProductId()).isEqualTo(101L);
        assertThat(top.get(0).getProductName()).isEqualTo("Premium Leather Bag");
        assertThat(top.get(0).getQuantitySold()).isEqualTo(15L);
        assertThat(top.get(0).getRevenueGenerated()).isEqualByComparingTo(BigDecimal.valueOf(15000.00));
    }

    @Test
    void getInventoryAlerts_shouldReturnLowStockProducts() {
        Product p = Product.builder().name("Wallet").stockQuantity(2).build();
        ProductDto pDto = ProductDto.builder().name("Wallet").stockQuantity(2).build();

        Mockito.when(entityManager.createQuery(contains("stockQuantity <= COALESCE(p.lowStockThreshold"), eq(Product.class)))
                .thenReturn(productQuery);
        Mockito.when(productQuery.setParameter(anyString(), any()))
                .thenReturn(productQuery);
        Mockito.when(productQuery.getResultList())
                .thenReturn(List.of(p));
        Mockito.when(productMapper.toDto(p))
                .thenReturn(pDto);

        List<ProductDto> alerts = dashboardService.getInventoryAlerts();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getName()).isEqualTo("Wallet");
        assertThat(alerts.get(0).getStockQuantity()).isEqualTo(2);
    }
}
