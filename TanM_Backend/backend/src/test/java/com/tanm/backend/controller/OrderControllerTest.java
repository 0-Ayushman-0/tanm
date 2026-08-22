package com.tanm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanm.backend.dto.OrderCreateRequest;
import com.tanm.backend.dto.OrderDto;
import com.tanm.backend.dto.OrderStatusUpdateRequest;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.enums.FulfillmentStatus;
import com.tanm.backend.enums.PaymentMethod;
import com.tanm.backend.enums.PaymentStatus;
import com.tanm.backend.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = {OrderController.class, AdminOrderController.class},
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = {com.tanm.backend.config.SecurityConfig.class, com.tanm.backend.config.JwtAuthenticationFilter.class}
        )
)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        orderDto = OrderDto.builder()
                .id(1L)
                .orderNumber("TNM-2026-99999999")
                .userEmail("john@example.com")
                .paymentStatus(PaymentStatus.PENDING)
                .fulfillmentStatus(FulfillmentStatus.PENDING)
                .paymentMethod(PaymentMethod.RAZORPAY)
                .grandTotal(BigDecimal.valueOf(210.00))
                .build();
    }

    @Test
    void createOrder_shouldReturnCreatedOrder() throws Exception {
        // Mock Authentication Context
        AppUser principal = AppUser.builder().email("john@example.com").build();
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal, null, Collections.emptyList()
                );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

        OrderCreateRequest request = OrderCreateRequest.builder()
                .shippingAddressId(5L)
                .billingAddressId(6L)
                .paymentMethod(PaymentMethod.RAZORPAY)
                .build();

        Mockito.when(orderService.createOrder(any(AppUser.class), any(OrderCreateRequest.class)))
                .thenReturn(orderDto);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("TNM-2026-99999999"))
                .andExpect(jsonPath("$.grandTotal").value(210.00));
    }

    @Test
    void getMyOrders_shouldReturnList() throws Exception {
        AppUser principal = AppUser.builder().email("john@example.com").build();
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal, null, Collections.emptyList()
                );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

        org.springframework.data.domain.Page<OrderDto> page = new org.springframework.data.domain.PageImpl<>(Collections.singletonList(orderDto));
        Mockito.when(orderService.getMyOrders(any(AppUser.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderNumber").value("TNM-2026-99999999"));
    }

    @Test
    void getOrderByNumber_shouldReturnDetails() throws Exception {
        AppUser principal = AppUser.builder().email("john@example.com").build();
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal, null, Collections.emptyList()
                );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

        Mockito.when(orderService.getOrderByNumber(eq("TNM-2026-99999999"), any(AppUser.class)))
                .thenReturn(orderDto);

        mockMvc.perform(get("/api/orders/TNM-2026-99999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("TNM-2026-99999999"));
    }

    @Test
    void mockPaymentCallback_shouldReturnUpdatedOrder() throws Exception {
        Mockito.when(orderService.processPaymentCallback(eq("TNM-2026-99999999"), eq("pay_xyz"), eq("ord_xyz"), eq("ref_xyz")))
                .thenReturn(orderDto);

        mockMvc.perform(post("/api/orders/TNM-2026-99999999/pay")
                        .param("paymentId", "pay_xyz")
                        .param("rpayOrderId", "ord_xyz")
                        .param("reference", "ref_xyz"))
                .andExpect(status().isOk());
    }

    @Test
    void cancelOrder_shouldReturnCancelledOrder() throws Exception {
        AppUser principal = AppUser.builder().email("john@example.com").build();
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal, null, Collections.emptyList()
                );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

        Mockito.when(orderService.cancelOrder(eq("TNM-2026-99999999"), any(AppUser.class)))
                .thenReturn(orderDto);

        mockMvc.perform(post("/api/orders/TNM-2026-99999999/cancel"))
                .andExpect(status().isOk());
    }

    // Admin Endpoints
    @Test
    void adminGetAllOrders_shouldReturnList() throws Exception {
        Mockito.when(orderService.getAllOrdersAdmin(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(Collections.singletonList(orderDto)));

        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderNumber").value("TNM-2026-99999999"));
    }

    @Test
    void adminUpdateStatus_shouldReturnUpdatedOrder() throws Exception {
        OrderStatusUpdateRequest request = OrderStatusUpdateRequest.builder()
                .fulfillmentStatus(FulfillmentStatus.SHIPPED)
                .remarks("Shipped via BlueDart")
                .build();

        Mockito.when(orderService.updateOrderStatusAdmin(eq("TNM-2026-99999999"), any(OrderStatusUpdateRequest.class)))
                .thenReturn(orderDto);

        mockMvc.perform(patch("/api/admin/orders/TNM-2026-99999999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
