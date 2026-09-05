package com.tanm.backend.entity;

import com.tanm.backend.common.BaseEntity;
import com.tanm.backend.enums.FulfillmentStatus;
import com.tanm.backend.enums.PaymentMethod;
import com.tanm.backend.enums.PaymentStatus;
import com.tanm.backend.enums.CouponType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_user", columnList = "user_id"),
        @Index(name = "idx_orders_number", columnList = "order_number", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    private AppUser user;

    @Column(name = "guest_token", length = 100)
    private String guestToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status", nullable = false)
    private FulfillmentStatus fulfillmentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    // Shipping Address Snapshot
    @Column(name = "shipping_full_name", nullable = false)
    private String shippingFullName;

    @Column(name = "shipping_phone_number", nullable = false)
    private String shippingPhoneNumber;

    @Column(name = "shipping_address_line1", nullable = false)
    private String shippingAddressLine1;

    @Column(name = "shipping_address_line2")
    private String shippingAddressLine2;

    @Column(name = "shipping_city", nullable = false)
    private String shippingCity;

    @Column(name = "shipping_state", nullable = false)
    private String shippingState;

    @Column(name = "shipping_postal_code", nullable = false)
    private String shippingPostalCode;

    @Column(name = "shipping_country", nullable = false)
    private String shippingCountry;

    // Billing Address Snapshot
    @Column(name = "billing_full_name", nullable = false)
    private String billingFullName;

    @Column(name = "billing_phone_number", nullable = false)
    private String billingPhoneNumber;

    @Column(name = "billing_address_line1", nullable = false)
    private String billingAddressLine1;

    @Column(name = "billing_address_line2")
    private String billingAddressLine2;

    @Column(name = "billing_city", nullable = false)
    private String billingCity;

    @Column(name = "billing_state", nullable = false)
    private String billingState;

    @Column(name = "billing_postal_code", nullable = false)
    private String billingPostalCode;

    @Column(name = "billing_country", nullable = false)
    private String billingCountry;

    // Pricing snapshot totals
    @Column(name = "subtotal", nullable = false, precision = 38, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "shipping_fee", nullable = false, precision = 38, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "tax_fee", nullable = false, precision = 38, scale = 2)
    private BigDecimal taxFee;

    @Column(name = "discount_amount", nullable = false, precision = 38, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "grand_total", nullable = false, precision = 38, scale = 2)
    private BigDecimal grandTotal;

    // Coupon snapshot
    @Column(name = "coupon_code")
    private String couponCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type")
    private CouponType couponType;

    @Column(name = "coupon_value", precision = 10, scale = 2)
    private BigDecimal couponValue;

    // Shipping metadata
    @Column(name = "shipping_method")
    private String shippingMethod;

    @Column(name = "carrier")
    private String carrier;

    @Column(name = "tracking_number")
    private String trackingNumber;

    // Razorpay specific Integration (nullable)
    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "payment_failure_reason")
    private String paymentFailureReason;



    // Milestone Timestamps
    @Column(name = "ordered_at")
    private LocalDateTime orderedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderTimeline> timeline = new ArrayList<>();
}
