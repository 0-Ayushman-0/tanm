package com.tanm.backend.service;

import com.tanm.backend.dto.InventoryAuditDto;
import com.tanm.backend.dto.InventoryStockAdjustmentRequest;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {
    InventoryAuditDto adjustStock(AppUser admin, InventoryStockAdjustmentRequest request);
    void logSystemStockChange(Product product, int prevStock, int newStock, String adjustmentType, String reason, String actor);
    Page<InventoryAuditDto> getAuditLogsForProduct(Long productId, Pageable pageable);
    Page<InventoryAuditDto> getAllAuditLogs(Pageable pageable);
}
