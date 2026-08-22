package com.tanm.backend.service.impl;

import com.tanm.backend.dto.InventoryAuditDto;
import com.tanm.backend.dto.InventoryStockAdjustmentRequest;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.entity.InventoryAuditLog;
import com.tanm.backend.entity.Product;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.repository.InventoryAuditLogRepository;
import com.tanm.backend.repository.ProductRepository;
import com.tanm.backend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final InventoryAuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public InventoryAuditDto adjustStock(AppUser admin, InventoryStockAdjustmentRequest request) {
        Product product = productRepository.findByIdForUpdate(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        int prevStock = product.getStockQuantity();
        int newStock = request.getNewStockQuantity();
        int quantityChanged = newStock - prevStock;

        product.setStockQuantity(newStock);
        productRepository.save(product);

        String actor = admin != null ? admin.getEmail() : "ADMIN";
        InventoryAuditLog auditLog = InventoryAuditLog.builder()
                .product(product)
                .previousStock(prevStock)
                .newStock(newStock)
                .quantityChanged(quantityChanged)
                .adjustmentType(request.getAdjustmentType())
                .reason(request.getReason() != null ? request.getReason() : "Manual admin stock adjustment")
                .adjustedBy(actor)
                .build();

        InventoryAuditLog savedLog = auditLogRepository.save(auditLog);
        log.info("📦 Stock adjusted for product [{}] SKU [{}]: {} -> {} ({}) by [{}]",
                product.getName(), product.getSku(), prevStock, newStock, request.getAdjustmentType(), actor);

        return toDto(savedLog);
    }

    @Override
    @Transactional
    public void logSystemStockChange(Product product, int prevStock, int newStock, String adjustmentType, String reason, String actor) {
        InventoryAuditLog auditLog = InventoryAuditLog.builder()
                .product(product)
                .previousStock(prevStock)
                .newStock(newStock)
                .quantityChanged(newStock - prevStock)
                .adjustmentType(adjustmentType)
                .reason(reason)
                .adjustedBy(actor != null ? actor : "SYSTEM")
                .build();
        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryAuditDto> getAuditLogsForProduct(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
        return auditLogRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryAuditDto> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toDto);
    }

    private InventoryAuditDto toDto(InventoryAuditLog log) {
        return InventoryAuditDto.builder()
                .id(log.getId())
                .productId(log.getProduct().getId())
                .productName(log.getProduct().getName())
                .productSku(log.getProduct().getSku())
                .previousStock(log.getPreviousStock())
                .newStock(log.getNewStock())
                .quantityChanged(log.getQuantityChanged())
                .adjustmentType(log.getAdjustmentType())
                .reason(log.getReason())
                .adjustedBy(log.getAdjustedBy())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
