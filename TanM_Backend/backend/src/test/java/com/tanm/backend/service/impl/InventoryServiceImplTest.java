package com.tanm.backend.service.impl;

import com.tanm.backend.dto.InventoryAuditDto;
import com.tanm.backend.dto.InventoryStockAdjustmentRequest;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.entity.InventoryAuditLog;
import com.tanm.backend.entity.Product;
import com.tanm.backend.repository.InventoryAuditLogRepository;
import com.tanm.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryAuditLogRepository auditLogRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private AppUser admin;
    private Product product;

    @BeforeEach
    void setUp() {
        admin = AppUser.builder().email("admin@tanm.com").build();
        admin.setId(1L);

        product = Product.builder().name("Duffle Bag").sku("DUFFLE-001").stockQuantity(10).build();
        product.setId(50L);
    }

    @Test
    void adjustStock_shouldUpdateStockAndCreateAuditLog() {
        InventoryStockAdjustmentRequest request = InventoryStockAdjustmentRequest.builder()
                .productId(50L)
                .newStockQuantity(35)
                .adjustmentType("RESTOCK")
                .reason("Supplier shipment received")
                .build();

        Mockito.when(productRepository.findByIdForUpdate(50L))
                .thenReturn(Optional.of(product));
        Mockito.when(auditLogRepository.save(any(InventoryAuditLog.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        InventoryAuditDto dto = inventoryService.adjustStock(admin, request);

        assertThat(product.getStockQuantity()).isEqualTo(35);
        assertThat(dto.getPreviousStock()).isEqualTo(10);
        assertThat(dto.getNewStock()).isEqualTo(35);
        assertThat(dto.getQuantityChanged()).isEqualTo(25);
        assertThat(dto.getAdjustmentType()).isEqualTo("RESTOCK");
        assertThat(dto.getAdjustedBy()).isEqualTo("admin@tanm.com");
    }
}
