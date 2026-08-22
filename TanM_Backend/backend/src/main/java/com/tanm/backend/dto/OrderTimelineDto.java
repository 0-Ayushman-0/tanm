package com.tanm.backend.dto;

import com.tanm.backend.enums.FulfillmentStatus;
import com.tanm.backend.enums.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderTimelineDto {

    private Long id;
    private FulfillmentStatus previousFulfillmentStatus;
    private FulfillmentStatus newFulfillmentStatus;
    private PaymentStatus previousPaymentStatus;
    private PaymentStatus newPaymentStatus;
    private String changedBy;
    private String remarks;
    private LocalDateTime timestamp;

    @com.fasterxml.jackson.annotation.JsonProperty("createdAt")
    public LocalDateTime getCreatedAt() {
        return timestamp;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("description")
    public String getDescription() {
        return remarks;
    }
}
