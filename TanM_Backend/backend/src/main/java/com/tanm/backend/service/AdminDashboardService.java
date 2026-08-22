package com.tanm.backend.service;

import com.tanm.backend.dto.DashboardSummaryDto;
import com.tanm.backend.dto.ProductDto;
import com.tanm.backend.dto.TopProductDto;

import java.util.List;
import java.util.Map;

public interface AdminDashboardService {

    DashboardSummaryDto getSummary();

    List<Map<String, Object>> getRevenueTrends();

    List<Map<String, Object>> getOrderTrends();

    List<TopProductDto> getTopProducts(int limit);

    List<ProductDto> getInventoryAlerts();
}
