package com.tanm.backend.entity;

import com.tanm.backend.common.BaseEntity;
import com.tanm.backend.enums.FulfillmentStatus;
import com.tanm.backend.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_timelines", indexes = {
        @Index(name = "idx_order_timelines_order", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderTimeline extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_fulfillment_status")
    private FulfillmentStatus previousFulfillmentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_fulfillment_status", nullable = false)
    private FulfillmentStatus newFulfillmentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_payment_status")
    private PaymentStatus previousPaymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_payment_status", nullable = false)
    private PaymentStatus newPaymentStatus;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}
