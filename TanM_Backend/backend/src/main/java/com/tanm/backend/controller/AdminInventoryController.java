package com.tanm.backend.controller;

import com.tanm.backend.dto.InventoryAuditDto;
import com.tanm.backend.dto.InventoryStockAdjustmentRequest;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/adjust")
    public ResponseEntity<InventoryAuditDto> adjustStock(
            @AuthenticationPrincipal AppUser admin,
            @Valid @RequestBody InventoryStockAdjustmentRequest request
    ) {
        return ResponseEntity.ok(inventoryService.adjustStock(admin, request));
    }

    @GetMapping("/audit")
    public ResponseEntity<Page<InventoryAuditDto>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(inventoryService.getAllAuditLogs(PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/audit/product/{productId}")
    public ResponseEntity<Page<InventoryAuditDto>> getAuditLogsForProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(inventoryService.getAuditLogsForProduct(productId, PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }
}
