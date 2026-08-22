package com.tanm.backend.dto;

import com.tanm.backend.enums.FulfillmentStatus;
import com.tanm.backend.enums.PaymentMethod;
import com.tanm.backend.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {

    private Long id;
    private String orderNumber;
    private String userEmail;
    private String userName;
    private PaymentStatus paymentStatus;
    private FulfillmentStatus fulfillmentStatus;
    private PaymentMethod paymentMethod;

    // Shipping Address Snapshot
    private String shippingFullName;
    private String shippingPhoneNumber;
    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCity;
    private String shippingState;
    private String shippingPostalCode;
    private String shippingCountry;

    // Billing Address Snapshot
    private String billingFullName;
    private String billingPhoneNumber;
    private String billingAddressLine1;
    private String billingAddressLine2;
    private String billingCity;
    private String billingState;
    private String billingPostalCode;
    private String billingCountry;

    // Financial details
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal taxFee;
    private BigDecimal discountAmount;
    private BigDecimal grandTotal;

    // Shipping details
    private String shippingMethod;
    private String carrier;
    private String trackingNumber;

    // Razorpay Integration details
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String paymentReference;
    private String paymentFailureReason;

    private String couponCode;

    // Milestone Timestamps
    private LocalDateTime orderedAt;
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime refundedAt;

    private List<OrderItemDto> items;
    private List<OrderTimelineDto> timeline;

    @com.fasterxml.jackson.annotation.JsonProperty("createdAt")
    public LocalDateTime getCreatedAt() {
        return orderedAt;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("customerName")
    public String getCustomerName() {
        if (userName != null && !userName.isBlank()) {
            return userName;
        }
        if (shippingFullName != null && !shippingFullName.isBlank()) {
            return shippingFullName;
        }
        if (billingFullName != null && !billingFullName.isBlank()) {
            return billingFullName;
        }
        return "Guest User";
    }
}
