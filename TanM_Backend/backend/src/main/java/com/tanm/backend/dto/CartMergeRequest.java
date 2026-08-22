package com.tanm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartMergeRequest {

    @NotBlank(message = "Guest token is required to merge carts")
    private String guestToken;
}
