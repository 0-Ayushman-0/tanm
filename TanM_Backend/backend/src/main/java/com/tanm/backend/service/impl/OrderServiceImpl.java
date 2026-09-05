package com.tanm.backend.service.impl;

import com.tanm.backend.dto.OrderCreateRequest;
import com.tanm.backend.dto.OrderDto;
import com.tanm.backend.dto.OrderStatusUpdateRequest;
import com.tanm.backend.entity.*;
import com.tanm.backend.enums.CartStatus;
import com.tanm.backend.enums.FulfillmentStatus;
import com.tanm.backend.enums.PaymentStatus;
import com.tanm.backend.enums.ProductStatus;
import com.tanm.backend.exception.BadRequestException;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.event.OrderPaidEvent;
import com.tanm.backend.event.OrderShippedEvent;
import com.tanm.backend.mapper.OrderMapper;
import com.tanm.backend.repository.*;
import com.tanm.backend.service.OrderService;
import com.tanm.backend.service.CouponService;
import com.tanm.backend.dto.CouponCalculationResponse;
import com.tanm.backend.enums.CouponType;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderTimelineRepository orderTimelineRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final CouponService couponService;
    private final Random random = new Random();

    @org.springframework.beans.factory.annotation.Value("${app.finance.tax-rate:0.05}")
    private BigDecimal taxRate;

    @org.springframework.beans.factory.annotation.Value("${app.finance.free-shipping-threshold:150.00}")
    private BigDecimal freeShippingThreshold;

    @org.springframework.beans.factory.annotation.Value("${app.finance.default-shipping-fee:10.00}")
    private BigDecimal defaultShippingFee;

    @Override
    @Transactional
    public OrderDto createOrder(AppUser user, String guestToken, OrderCreateRequest request) {
        // 1. Fetch Cart with Items
        Cart cart;
        if (user != null) {
            List<Cart> userCarts = cartRepository.findByUserAndStatusWithItemsList(user, CartStatus.ACTIVE);
            if (userCarts.isEmpty()) {
                throw new BadRequestException("No active shopping cart found for user");
            }
            cart = userCarts.get(0);
        } else if (guestToken != null && !guestToken.isBlank()) {
            List<Cart> guestCarts = cartRepository.findByGuestTokenAndStatusWithItemsList(guestToken, CartStatus.ACTIVE);
            if (guestCarts.isEmpty()) {
                throw new BadRequestException("No active shopping cart found for guest");
            }
            cart = guestCarts.get(0);
        } else {
            throw new BadRequestException("User authentication or Guest-Token header required to place an order");
        }

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Active shopping cart is empty");
        }

        // 2. Fetch Addresses
        Address shippingAddr = addressRepository.findByIdAndIsDeletedFalse(request.getShippingAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found with id: " + request.getShippingAddressId()));

        Address billingAddr = addressRepository.findByIdAndIsDeletedFalse(request.getBillingAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Billing address not found with id: " + request.getBillingAddressId()));

        // 3. Pre-validate stock and product availability (catalog checks)
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product.isDeleted() || !product.isActive() || product.getStatus() != ProductStatus.PUBLISHED) {
                throw new BadRequestException("Cannot checkout cart; contains discontinued product: " + product.getName());
            }
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product '" + product.getName() + "'; only " + product.getStockQuantity() + " remaining");
            }
            BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);
        }

        // 4. Calculate Financial Fields
        BigDecimal shippingFee = subtotal.compareTo(freeShippingThreshold) >= 0 ? BigDecimal.ZERO : defaultShippingFee;
        BigDecimal taxFee = subtotal.multiply(taxRate);
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        String couponCode = null;
        CouponType couponType = null;
        BigDecimal couponValue = null;

        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            CouponCalculationResponse calc = couponService.calculateDiscount(request.getCouponCode(), user);
            couponCode = calc.getCode();
            couponType = calc.getType();
            couponValue = calc.getCouponValue();
            discountAmount = calc.getDiscountAmount();
            if (couponType == CouponType.FREE_SHIPPING) {
                discountAmount = shippingFee;
            }
        }

        BigDecimal grandTotal = subtotal.add(shippingFee).add(taxFee).subtract(discountAmount);
        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) {
            grandTotal = BigDecimal.ZERO;
        }

        // 5. Generate Order Number
        String orderNumber = String.format("TNM-%d-%08d", Year.now().getValue(), Math.abs(random.nextInt(100000000)));

        // 6. Create Order Entity
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .guestToken(user == null ? guestToken : null)
                .paymentStatus(PaymentStatus.PENDING)
                .fulfillmentStatus(FulfillmentStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .shippingFullName(shippingAddr.getFullName())
                .shippingPhoneNumber(shippingAddr.getPhoneNumber())
                .shippingAddressLine1(shippingAddr.getAddressLine1())
                .shippingAddressLine2(shippingAddr.getAddressLine2())
                .shippingCity(shippingAddr.getCity())
                .shippingState(shippingAddr.getState())
                .shippingPostalCode(shippingAddr.getPostalCode())
                .shippingCountry(shippingAddr.getCountry())
                .billingFullName(billingAddr.getFullName())
                .billingPhoneNumber(billingAddr.getPhoneNumber())
                .billingAddressLine1(billingAddr.getAddressLine1())
                .billingAddressLine2(billingAddr.getAddressLine2())
                .billingCity(billingAddr.getCity())
                .billingState(billingAddr.getState())
                .billingPostalCode(billingAddr.getPostalCode())
                .billingCountry(billingAddr.getCountry())
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .taxFee(taxFee)
                .discountAmount(discountAmount)
                .grandTotal(grandTotal)
                .couponCode(couponCode)
                .couponType(couponType)
                .couponValue(couponValue)
                .orderedAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .timeline(new ArrayList<>())
                .build();

        // 7. Map CartItems to OrderItems
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            String primaryImage = product.getImages() != null ?
                    product.getImages().stream()
                            .filter(ProductImage::isPrimary)
                            .findFirst()
                            .map(ProductImage::getImageUrl)
                            .orElse(null) : null;

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .sku(product.getSku())
                    .slug(product.getSlug())
                    .primaryImageUrl(primaryImage)
                    .color(product.getColor())
                    .leatherType(product.getLeatherType())
                    .quantity(item.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();

            order.getItems().add(orderItem);
        }

        // 8. Add initial Order Timeline
        OrderTimeline timelineEvent = OrderTimeline.builder()
                .order(order)
                .previousFulfillmentStatus(null)
                .newFulfillmentStatus(FulfillmentStatus.PENDING)
                .previousPaymentStatus(null)
                .newPaymentStatus(PaymentStatus.PENDING)
                .changedBy("SYSTEM")
                .remarks("Order checkout initiated. Placed in pending state.")
                .timestamp(LocalDateTime.now())
                .build();
        order.getTimeline().add(timelineEvent);

        // Cart remains ACTIVE until payment is completed successfully

        // 10. Persist Order
        Order saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getMyOrders(AppUser user, String guestToken, Pageable pageable) {
        if (user != null) {
            return orderRepository.findByUserAndIsDeletedFalse(user, pageable).map(orderMapper::toDto);
        } else if (guestToken != null && !guestToken.isBlank()) {
            return orderRepository.findByGuestTokenAndIsDeletedFalse(guestToken, pageable).map(orderMapper::toDto);
        }
        return Page.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderByNumber(String orderNumber, AppUser user, String guestToken) {
        Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));

        validateOrderOwnership(order, user, guestToken, orderNumber);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto cancelOrder(String orderNumber, AppUser user, String guestToken) {
        Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));

        validateOrderOwnership(order, user, guestToken, orderNumber);

        if (order.getFulfillmentStatus() != FulfillmentStatus.PENDING &&
            order.getFulfillmentStatus() != FulfillmentStatus.CONFIRMED) {
            throw new BadRequestException("Cannot cancel order in current fulfillment state: " + order.getFulfillmentStatus());
        }

        FulfillmentStatus prevFulfillment = order.getFulfillmentStatus();
        PaymentStatus prevPayment = order.getPaymentStatus();

        order.setFulfillmentStatus(FulfillmentStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());

        // Stock replenishment if payment was completed
        if (prevPayment == PaymentStatus.PAID) {
            for (OrderItem item : order.getItems()) {
                Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + item.getProduct().getId()));
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
            order.setPaymentStatus(PaymentStatus.REFUNDED);
            order.setRefundedAt(LocalDateTime.now());
        }

        OrderTimeline timelineEvent = OrderTimeline.builder()
                .order(order)
                .previousFulfillmentStatus(prevFulfillment)
                .newFulfillmentStatus(FulfillmentStatus.CANCELLED)
                .previousPaymentStatus(prevPayment)
                .newPaymentStatus(order.getPaymentStatus())
                .changedBy("CUSTOMER")
                .remarks("Order cancelled by customer.")
                .timestamp(LocalDateTime.now())
                .build();
        orderTimelineRepository.save(timelineEvent);

        Order saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    @Override
    @Transactional
    public OrderDto processPaymentCallback(String orderNumber, String razorpayPaymentId, String razorpayOrderId, String paymentReference) {
        Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return orderMapper.toDto(order);
        }
        if (order.getFulfillmentStatus() == FulfillmentStatus.CANCELLED) {
            throw new BadRequestException("Cannot pay for cancelled order");
        }

        FulfillmentStatus prevFulfillment = order.getFulfillmentStatus();
        PaymentStatus prevPayment = order.getPaymentStatus();

        // Lock & update stock
        try {
            for (OrderItem item : order.getItems()) {
                Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + item.getProduct().getId()));

                if (product.isDeleted() || !product.isActive() || product.getStatus() != ProductStatus.PUBLISHED) {
                    throw new BadRequestException("Product discontinued during checkout payment: " + product.getName());
                }

                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new BadRequestException("Insufficient stock for product '" + product.getName() + "'; only " + product.getStockQuantity() + " remaining");
                }

                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                productRepository.save(product);
            }
        } catch (BadRequestException ex) {
            // Transition Payment to FAILED
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setPaymentFailureReason(ex.getMessage());

            OrderTimeline failEvent = OrderTimeline.builder()
                    .order(order)
                    .previousFulfillmentStatus(prevFulfillment)
                    .newFulfillmentStatus(prevFulfillment)
                    .previousPaymentStatus(prevPayment)
                    .newPaymentStatus(PaymentStatus.FAILED)
                    .changedBy("SYSTEM")
                    .remarks("Payment failed: " + ex.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            orderTimelineRepository.save(failEvent);
            orderRepository.save(order);
            throw ex;
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setFulfillmentStatus(FulfillmentStatus.CONFIRMED);
        order.setPaidAt(LocalDateTime.now());
        order.setRazorpayOrderId(razorpayOrderId);
        order.setRazorpayPaymentId(razorpayPaymentId);
        order.setPaymentReference(paymentReference);

        OrderTimeline timelineEvent = OrderTimeline.builder()
                .order(order)
                .previousFulfillmentStatus(prevFulfillment)
                .newFulfillmentStatus(FulfillmentStatus.CONFIRMED)
                .previousPaymentStatus(prevPayment)
                .newPaymentStatus(PaymentStatus.PAID)
                .changedBy("SYSTEM")
                .remarks("Payment callback processed. Status updated to PAID. Fulfillment updated to CONFIRMED.")
                .timestamp(LocalDateTime.now())
                .build();
        orderTimelineRepository.save(timelineEvent);

        // Redeem coupon on successful payment processing
        if (order.getCouponCode() != null && !order.getCouponCode().isBlank()) {
            couponService.redeemCoupon(order.getCouponCode(), order.getUser(), order);
        }

        // Invalidate Cart upon successful payment completion
        if (order.getUser() != null) {
            List<Cart> activeCarts = cartRepository.findByUserAndStatusWithItemsList(order.getUser(), CartStatus.ACTIVE);
            for (Cart c : activeCarts) {
                c.setStatus(CartStatus.CONVERTED);
                cartRepository.save(c);
            }
        } else if (order.getGuestToken() != null) {
            List<Cart> activeCarts = cartRepository.findByGuestTokenAndStatusWithItemsList(order.getGuestToken(), CartStatus.ACTIVE);
            for (Cart c : activeCarts) {
                c.setStatus(CartStatus.CONVERTED);
                cartRepository.save(c);
            }
        }

        Order saved = orderRepository.save(order);
        OrderDto savedDto = orderMapper.toDto(saved);

        // Publish OrderPaidEvent to decouple order notification flow
        eventPublisher.publishEvent(new OrderPaidEvent(this, savedDto));

        return savedDto;
    }

    // Admin Operations
    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getAllOrdersAdmin(Pageable pageable) {
        return orderRepository.findAllWithItems(pageable)
                .map(orderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderByNumberAdmin(String orderNumber) {
        Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto updateOrderStatusAdmin(String orderNumber, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));

        FulfillmentStatus prevFulfillment = order.getFulfillmentStatus();
        PaymentStatus prevPayment = order.getPaymentStatus();

        if (request.getCarrier() != null) {
            order.setCarrier(request.getCarrier());
        }
        if (request.getTrackingNumber() != null) {
            order.setTrackingNumber(request.getTrackingNumber());
        }

        // Fulfillment status transition logic
        if (request.getFulfillmentStatus() != null && request.getFulfillmentStatus() != prevFulfillment) {
            if (prevFulfillment == FulfillmentStatus.CANCELLED || prevFulfillment == FulfillmentStatus.DELIVERED) {
                throw new BadRequestException("Cannot modify status for orders already marked as " + prevFulfillment);
            }

            order.setFulfillmentStatus(request.getFulfillmentStatus());

            if (request.getFulfillmentStatus() == FulfillmentStatus.SHIPPED) {
                order.setShippedAt(LocalDateTime.now());
            } else if (request.getFulfillmentStatus() == FulfillmentStatus.DELIVERED) {
                order.setDeliveredAt(LocalDateTime.now());
            } else if (request.getFulfillmentStatus() == FulfillmentStatus.CANCELLED) {
                order.setCancelledAt(LocalDateTime.now());
                // Replenish stock if already paid
                if (order.getPaymentStatus() == PaymentStatus.PAID) {
                    for (OrderItem item : order.getItems()) {
                        Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + item.getProduct().getId()));
                        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                        productRepository.save(product);
                    }
                    order.setPaymentStatus(PaymentStatus.REFUNDED);
                    order.setRefundedAt(LocalDateTime.now());
                }
            }
        }

        // Payment status transition logic
        if (request.getPaymentStatus() != null && request.getPaymentStatus() != prevPayment) {
            if (prevPayment == PaymentStatus.PAID && request.getPaymentStatus() == PaymentStatus.PENDING) {
                throw new BadRequestException("Cannot revert paid orders back to pending");
            }

            order.setPaymentStatus(request.getPaymentStatus());

            if (request.getPaymentStatus() == PaymentStatus.PAID) {
                order.setPaidAt(LocalDateTime.now());
                // Perform stock decrement if not already done (re-checking transition)
                if (prevPayment == PaymentStatus.PENDING || prevPayment == PaymentStatus.FAILED) {
                    for (OrderItem item : order.getItems()) {
                        Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + item.getProduct().getId()));
                        if (product.getStockQuantity() < item.getQuantity()) {
                            throw new BadRequestException("Insufficient stock to mark order paid for product: " + product.getName());
                        }
                        product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                        productRepository.save(product);
                    }
                }
            } else if (request.getPaymentStatus() == PaymentStatus.REFUNDED) {
                order.setRefundedAt(LocalDateTime.now());
                // Replenish stock if we refund
                for (OrderItem item : order.getItems()) {
                    Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + item.getProduct().getId()));
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        OrderTimeline timelineEvent = OrderTimeline.builder()
                .order(order)
                .previousFulfillmentStatus(prevFulfillment)
                .newFulfillmentStatus(order.getFulfillmentStatus())
                .previousPaymentStatus(prevPayment)
                .newPaymentStatus(order.getPaymentStatus())
                .changedBy("ADMIN")
                .remarks(request.getRemarks() != null ? request.getRemarks() : "Order status updated by admin.")
                .timestamp(LocalDateTime.now())
                .build();
        orderTimelineRepository.save(timelineEvent);

        Order saved = orderRepository.save(order);
        OrderDto savedDto = orderMapper.toDto(saved);

        // Publish OrderShippedEvent if status transitions to SHIPPED
        if (request.getFulfillmentStatus() == FulfillmentStatus.SHIPPED) {
            eventPublisher.publishEvent(new OrderShippedEvent(this, savedDto));
        }

        return savedDto;
    }

    private void validateOrderOwnership(Order order, AppUser user, String guestToken, String orderNumber) {
        if (user != null && order.getUser() != null && order.getUser().getEmail().equals(user.getEmail())) {
            return;
        }
        if (guestToken != null && !guestToken.isBlank() && guestToken.equals(order.getGuestToken())) {
            return;
        }
        throw new ResourceNotFoundException("Order not found with number: " + orderNumber);
    }
}
