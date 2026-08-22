package com.tanm.backend.dto;

import com.tanm.backend.enums.FulfillmentStatus;
import com.tanm.backend.enums.PaymentStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdateRequest {

    private FulfillmentStatus fulfillmentStatus;
    private PaymentStatus paymentStatus;
    private String remarks;
    private String carrier;
    private String trackingNumber;
}
