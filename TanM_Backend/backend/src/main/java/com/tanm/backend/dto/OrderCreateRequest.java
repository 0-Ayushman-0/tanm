package com.tanm.backend.dto;

import com.tanm.backend.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateRequest {

    @NotNull(message = "Shipping address is required")
    private Long shippingAddressId;

    @NotNull(message = "Billing address is required")
    private Long billingAddressId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String couponCode;
}
