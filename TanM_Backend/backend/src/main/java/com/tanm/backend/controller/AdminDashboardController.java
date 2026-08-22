package com.tanm.backend.controller;

import com.tanm.backend.dto.DashboardSummaryDto;
import com.tanm.backend.dto.ProductDto;
import com.tanm.backend.dto.TopProductDto;
import com.tanm.backend.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/revenue")
    public ResponseEntity<List<Map<String, Object>>> getRevenueTrends() {
        return ResponseEntity.ok(dashboardService.getRevenueTrends());
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Map<String, Object>>> getOrderTrends() {
        return ResponseEntity.ok(dashboardService.getOrderTrends());
    }

    @GetMapping("/products")
    public ResponseEntity<List<TopProductDto>> getTopProducts(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(dashboardService.getTopProducts(limit));
    }

    @GetMapping("/inventory-alerts")
    public ResponseEntity<List<ProductDto>> getInventoryAlerts() {
        return ResponseEntity.ok(dashboardService.getInventoryAlerts());
    }
}
