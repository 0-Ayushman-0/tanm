package com.tanm.backend.controller;

import com.tanm.backend.dto.OrderCreateRequest;
import com.tanm.backend.dto.OrderDto;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.exception.BadRequestException;
import com.tanm.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            @Valid @RequestBody OrderCreateRequest request
    ) {
        AppUser user = getAuthenticatedUser();
        if (user == null) {
            throw new BadRequestException("Please sign in or create an account to complete your order.");
        }
        OrderDto created = orderService.createOrder(user, request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<OrderDto>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestHeader(value = "Guest-Token", required = false) String guestToken
    ) {
        AppUser user = getAuthenticatedUser();
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
                ? org.springframework.data.domain.Sort.by(sortBy).descending()
                : org.springframework.data.domain.Sort.by(sortBy).ascending();
        return ResponseEntity.ok(orderService.getMyOrders(user, guestToken, org.springframework.data.domain.PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderDto> getOrderByNumber(
            @PathVariable String orderNumber,
            @RequestHeader(value = "Guest-Token", required = false) String guestToken
    ) {
        AppUser user = getAuthenticatedUser();
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber, user, guestToken));
    }

    @PostMapping("/{orderNumber}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(
            @PathVariable String orderNumber,
            @RequestHeader(value = "Guest-Token", required = false) String guestToken
    ) {
        AppUser user = getAuthenticatedUser();
        return ResponseEntity.ok(orderService.cancelOrder(orderNumber, user, guestToken));
    }

    // Mock payment simulation endpoint
    @PostMapping("/{orderNumber}/pay")
    public ResponseEntity<OrderDto> mockPaymentCallback(
            @PathVariable String orderNumber,
            @RequestParam(required = false, defaultValue = "pay_mock_123") String paymentId,
            @RequestParam(required = false, defaultValue = "ord_mock_123") String rpayOrderId,
            @RequestParam(required = false, defaultValue = "ref_mock_123") String reference) {
        OrderDto updated = orderService.processPaymentCallback(orderNumber, paymentId, rpayOrderId, reference);
        return ResponseEntity.ok(updated);
    }

    private AppUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AppUser) {
            return (AppUser) authentication.getPrincipal();
        }
        return null;
    }
}
