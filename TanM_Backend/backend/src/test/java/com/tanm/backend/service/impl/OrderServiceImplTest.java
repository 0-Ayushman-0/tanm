package com.tanm.backend.service.impl;

import com.tanm.backend.dto.OrderCreateRequest;
import com.tanm.backend.dto.OrderDto;
import com.tanm.backend.dto.OrderStatusUpdateRequest;
import com.tanm.backend.entity.*;
import com.tanm.backend.enums.CartStatus;
import com.tanm.backend.enums.FulfillmentStatus;
import com.tanm.backend.enums.PaymentMethod;
import com.tanm.backend.enums.PaymentStatus;
import com.tanm.backend.enums.ProductStatus;
import com.tanm.backend.exception.BadRequestException;
import com.tanm.backend.mapper.OrderMapper;
import com.tanm.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderTimelineRepository orderTimelineRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Mock
    private com.tanm.backend.service.CouponService couponService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private AppUser user;
    private Product product;
    private Cart cart;
    private Address shippingAddr;
    private Address billingAddr;
    private Order order;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        user = AppUser.builder().email("john@example.com").build();
        user.setId(10L);

        product = Product.builder()
                .name("Tan Purse")
                .sku("AC-PURSE-01")
                .slug("tan-purse")
                .price(BigDecimal.valueOf(100.00))
                .status(ProductStatus.PUBLISHED)
                .build();
        product.setId(200L);
        product.setActive(true);
        product.setDeleted(false);

        CartItem cartItem = CartItem.builder()
                .product(product)
                .quantity(2)
                .build();

        cart = Cart.builder()
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>(Collections.singletonList(cartItem)))
                .build();
        cart.setId(5L);

        shippingAddr = Address.builder()
                .fullName("John Shipping")
                .phoneNumber("+123")
                .addressLine1("123 Shipping Lane")
                .city("NYC")
                .state("NY")
                .postalCode("10001")
                .country("US")
                .build();
        shippingAddr.setId(1L);

        billingAddr = Address.builder()
                .fullName("John Billing")
                .phoneNumber("+123")
                .addressLine1("456 Billing Ave")
                .city("NYC")
                .state("NY")
                .postalCode("10001")
                .country("US")
                .build();
        billingAddr.setId(2L);

        order = Order.builder()
                .orderNumber("TNM-2026-12345678")
                .user(user)
                .paymentStatus(PaymentStatus.PENDING)
                .fulfillmentStatus(FulfillmentStatus.PENDING)
                .paymentMethod(PaymentMethod.RAZORPAY)
                .subtotal(BigDecimal.valueOf(200.00))
                .shippingFee(BigDecimal.ZERO)
                .taxFee(BigDecimal.TEN)
                .grandTotal(BigDecimal.valueOf(210.00))
                .items(new ArrayList<>())
                .timeline(new ArrayList<>())
                .build();
        order.setId(15L);

        orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .productName("Tan Purse")
                .sku("AC-PURSE-01")
                .slug("tan-purse")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(100.00))
                .subtotal(BigDecimal.valueOf(200.00))
                .build();
        orderItem.setId(30L);
        order.getItems().add(orderItem);
    }

    @Test
    void createOrder_shouldInitOrderWithoutDecrementingStock() {
        // Arrange
        product.setStockQuantity(10);
        OrderCreateRequest req = OrderCreateRequest.builder()
                .shippingAddressId(1L)
                .billingAddressId(2L)
                .paymentMethod(PaymentMethod.RAZORPAY)
                .build();

        Mockito.when(cartRepository.findByUserAndStatusWithItems(eq(user), eq(CartStatus.ACTIVE)))
                .thenReturn(Optional.of(cart));
        Mockito.when(addressRepository.findByIdAndUserAndIsDeletedFalse(eq(1L), eq(user)))
                .thenReturn(Optional.of(shippingAddr));
        Mockito.when(addressRepository.findByIdAndUserAndIsDeletedFalse(eq(2L), eq(user)))
                .thenReturn(Optional.of(billingAddr));
        Mockito.when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        Mockito.when(orderMapper.toDto(any(Order.class))).thenReturn(OrderDto.builder().orderNumber("TNM-2026-12345678").build());

        // Act
        OrderDto result = orderService.createOrder(user, req);

        // Assert
        assertNotNull(result);
        assertEquals("TNM-2026-12345678", result.getOrderNumber());
        assertEquals(10, product.getStockQuantity()); // Stock unchanged at checkout init!
        assertEquals(CartStatus.ACTIVE, cart.getStatus()); // Cart remains active until paid

        Mockito.verify(orderRepository).save(any(Order.class));
    }

    @Test
    void processPaymentCallback_onSuccess_shouldDecrementStock() {
        // Arrange
        product.setStockQuantity(10);
        Mockito.when(orderRepository.findByOrderNumberWithItems("TNM-2026-12345678"))
                .thenReturn(Optional.of(order));
        Mockito.when(productRepository.findByIdForUpdate(200L))
                .thenReturn(Optional.of(product));
        Mockito.when(cartRepository.findByUserAndStatus(eq(user), eq(CartStatus.ACTIVE)))
                .thenReturn(Optional.of(cart));
        Mockito.when(orderRepository.save(any(Order.class)))
                .thenAnswer(i -> i.getArgument(0));
        Mockito.when(orderMapper.toDto(any(Order.class)))
                .thenReturn(OrderDto.builder().userEmail("user@example.com").build());

        // Act
        orderService.processPaymentCallback("TNM-2026-12345678", "pay_xyz", "ord_xyz", "ref_xyz");

        // Assert
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        assertEquals(FulfillmentStatus.CONFIRMED, order.getFulfillmentStatus());
        assertEquals(8, product.getStockQuantity()); // Decremented from 10 to 8!
        assertEquals(CartStatus.CONVERTED, cart.getStatus()); // Cart converted post-payment

        Mockito.verify(productRepository).save(product);
        Mockito.verify(cartRepository).save(cart);
        Mockito.verify(orderRepository).save(order);
        Mockito.verify(orderTimelineRepository).save(any(OrderTimeline.class));
    }

    @Test
    void processPaymentCallback_insufficientStock_shouldSetPaymentFailedAndKeepStock() {
        // Arrange
        product.setStockQuantity(1); // Order requests 2
        Mockito.when(orderRepository.findByOrderNumberWithItems("TNM-2026-12345678"))
                .thenReturn(Optional.of(order));
        Mockito.when(productRepository.findByIdForUpdate(200L))
                .thenReturn(Optional.of(product));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                orderService.processPaymentCallback("TNM-2026-12345678", "pay_xyz", "ord_xyz", "ref_xyz")
        );
        assertTrue(ex.getMessage().contains("Insufficient stock"));
        assertEquals(PaymentStatus.FAILED, order.getPaymentStatus()); // Payment status transitions to FAILED
        assertEquals(1, product.getStockQuantity()); // Stock untouched

        Mockito.verify(orderRepository).save(order);
        Mockito.verify(orderTimelineRepository).save(any(OrderTimeline.class));
    }

    @Test
    void cancelOrder_whenPaid_shouldReplenishStockAndRefund() {
        // Arrange
        product.setStockQuantity(8);
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setFulfillmentStatus(FulfillmentStatus.CONFIRMED);

        Mockito.when(orderRepository.findByOrderNumberWithItems("TNM-2026-12345678"))
                .thenReturn(Optional.of(order));
        Mockito.when(productRepository.findByIdForUpdate(200L))
                .thenReturn(Optional.of(product));
        Mockito.when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        orderService.cancelOrder("TNM-2026-12345678", user);

        // Assert
        assertEquals(FulfillmentStatus.CANCELLED, order.getFulfillmentStatus());
        assertEquals(PaymentStatus.REFUNDED, order.getPaymentStatus());
        assertEquals(10, product.getStockQuantity()); // Restored back to 10!

        Mockito.verify(productRepository).save(product);
        Mockito.verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatusAdmin_deliveredToCancelled_shouldThrowBadRequestException() {
        // Arrange
        order.setFulfillmentStatus(FulfillmentStatus.DELIVERED);
        Mockito.when(orderRepository.findByOrderNumberWithItems("TNM-2026-12345678"))
                .thenReturn(Optional.of(order));

        OrderStatusUpdateRequest req = OrderStatusUpdateRequest.builder()
                .fulfillmentStatus(FulfillmentStatus.CANCELLED)
                .build();

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                orderService.updateOrderStatusAdmin("TNM-2026-12345678", req)
        );
        assertTrue(ex.getMessage().contains("Cannot modify status for orders already marked as DELIVERED"));
    }

    @Test
    void processPaymentCallback_alreadyPaid_shouldReturnDtoIdempotently() {
        // Arrange
        order.setPaymentStatus(PaymentStatus.PAID);
        Mockito.when(orderRepository.findByOrderNumberWithItems("TNM-2026-12345678"))
                .thenReturn(Optional.of(order));
        Mockito.when(orderMapper.toDto(eq(order)))
                .thenReturn(OrderDto.builder().build());

        // Act
        OrderDto result = orderService.processPaymentCallback("TNM-2026-12345678", "pay_xyz", "ord_xyz", "ref_xyz");

        // Assert
        assertNotNull(result);
        Mockito.verify(orderMapper).toDto(order);
        Mockito.verifyNoInteractions(productRepository);
    }
}
