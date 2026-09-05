package com.tanm.backend.service;

import com.tanm.backend.dto.OrderCreateRequest;
import com.tanm.backend.dto.OrderDto;
import com.tanm.backend.dto.OrderStatusUpdateRequest;
import com.tanm.backend.entity.AppUser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDto createOrder(AppUser user, String guestToken, OrderCreateRequest request);
    Page<OrderDto> getMyOrders(AppUser user, String guestToken, Pageable pageable);
    OrderDto getOrderByNumber(String orderNumber, AppUser user, String guestToken);
    OrderDto cancelOrder(String orderNumber, AppUser user, String guestToken);
    OrderDto processPaymentCallback(String orderNumber, String razorpayPaymentId, String razorpayOrderId, String paymentReference);

    default OrderDto createOrder(AppUser user, OrderCreateRequest request) { return createOrder(user, null, request); }
    default Page<OrderDto> getMyOrders(AppUser user, Pageable pageable) { return getMyOrders(user, null, pageable); }
    default OrderDto getOrderByNumber(String orderNumber, AppUser user) { return getOrderByNumber(orderNumber, user, null); }
    default OrderDto cancelOrder(String orderNumber, AppUser user) { return cancelOrder(orderNumber, user, null); }
    
    // Admin operations
    Page<OrderDto> getAllOrdersAdmin(Pageable pageable);
    OrderDto getOrderByNumberAdmin(String orderNumber);
    OrderDto updateOrderStatusAdmin(String orderNumber, OrderStatusUpdateRequest request);
}
