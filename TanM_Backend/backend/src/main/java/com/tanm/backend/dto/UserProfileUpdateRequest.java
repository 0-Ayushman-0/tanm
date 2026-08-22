package com.tanm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileUpdateRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    private String phoneNumber;
}
