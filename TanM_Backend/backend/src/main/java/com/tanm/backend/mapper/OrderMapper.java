package com.tanm.backend.mapper;

import com.tanm.backend.dto.OrderDto;
import com.tanm.backend.dto.OrderItemDto;
import com.tanm.backend.dto.OrderTimelineDto;
import com.tanm.backend.entity.Order;
import com.tanm.backend.entity.OrderItem;
import com.tanm.backend.entity.OrderTimeline;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderDto toDto(Order order) {
        if (order == null) {
            return null;
        }

        List<OrderItemDto> items = order.getItems() != null ?
                order.getItems().stream()
                        .map(this::toDto)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        List<OrderTimelineDto> timeline = order.getTimeline() != null ?
                order.getTimeline().stream()
                        .map(this::toDto)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        String userEmail = null;
        String userName = null;
        if (order.getUser() != null && org.hibernate.Hibernate.isInitialized(order.getUser())) {
            try {
                userEmail = order.getUser().getEmail();
                String fn = order.getUser().getFirstName() != null ? order.getUser().getFirstName() : "";
                String ln = order.getUser().getLastName() != null ? order.getUser().getLastName() : "";
                userName = (fn + " " + ln).trim();
            } catch (Exception ignored) {
                userEmail = null;
                userName = null;
            }
        }

        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userEmail(userEmail)
                .userName(userName)
                .paymentStatus(order.getPaymentStatus())
                .fulfillmentStatus(order.getFulfillmentStatus())
                .paymentMethod(order.getPaymentMethod())
                .shippingFullName(order.getShippingFullName())
                .shippingPhoneNumber(order.getShippingPhoneNumber())
                .shippingAddressLine1(order.getShippingAddressLine1())
                .shippingAddressLine2(order.getShippingAddressLine2())
                .shippingCity(order.getShippingCity())
                .shippingState(order.getShippingState())
                .shippingPostalCode(order.getShippingPostalCode())
                .shippingCountry(order.getShippingCountry())
                .billingFullName(order.getBillingFullName())
                .billingPhoneNumber(order.getBillingPhoneNumber())
                .billingAddressLine1(order.getBillingAddressLine1())
                .billingAddressLine2(order.getBillingAddressLine2())
                .billingCity(order.getBillingCity())
                .billingState(order.getBillingState())
                .billingPostalCode(order.getBillingPostalCode())
                .billingCountry(order.getBillingCountry())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .taxFee(order.getTaxFee())
                .discountAmount(order.getDiscountAmount())
                .grandTotal(order.getGrandTotal())
                .shippingMethod(order.getShippingMethod())
                .carrier(order.getCarrier())
                .trackingNumber(order.getTrackingNumber())
                .razorpayOrderId(order.getRazorpayOrderId())
                .razorpayPaymentId(order.getRazorpayPaymentId())
                .paymentReference(order.getPaymentReference())
                .paymentFailureReason(order.getPaymentFailureReason())
                .couponCode(order.getCouponCode())
                .orderedAt(order.getOrderedAt())
                .paidAt(order.getPaidAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .refundedAt(order.getRefundedAt())
                .items(items)
                .timeline(timeline)
                .build();
    }

    public OrderItemDto toDto(OrderItem item) {
        if (item == null) {
            return null;
        }
        return OrderItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProductName())
                .sku(item.getSku())
                .slug(item.getSlug())
                .primaryImageUrl(item.getPrimaryImageUrl())
                .color(item.getColor())
                .leatherType(item.getLeatherType())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }

    public OrderTimelineDto toDto(OrderTimeline event) {
        if (event == null) {
            return null;
        }
        return OrderTimelineDto.builder()
                .id(event.getId())
                .previousFulfillmentStatus(event.getPreviousFulfillmentStatus())
                .newFulfillmentStatus(event.getNewFulfillmentStatus())
                .previousPaymentStatus(event.getPreviousPaymentStatus())
                .newPaymentStatus(event.getNewPaymentStatus())
                .changedBy(event.getChangedBy())
                .remarks(event.getRemarks())
                .timestamp(event.getTimestamp())
                .build();
    }
}
