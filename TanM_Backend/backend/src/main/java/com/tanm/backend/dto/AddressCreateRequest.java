package com.tanm.backend.dto;

import com.tanm.backend.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressCreateRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be less than 100 characters")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^$|^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Size(max = 50, message = "Label must be less than 50 characters")
    private String label;

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 150, message = "Address line 1 must be less than 150 characters")
    private String addressLine1;

    @Size(max = 150, message = "Address line 2 must be less than 150 characters")
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must be less than 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must be less than 100 characters")
    private String state;

    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code must be less than 20 characters")
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must be less than 100 characters")
    private String country;

    @Builder.Default
    private AddressType addressType = AddressType.SHIPPING;

    private boolean isDefault;
}
