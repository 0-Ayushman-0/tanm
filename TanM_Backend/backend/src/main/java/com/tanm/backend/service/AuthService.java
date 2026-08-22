package com.tanm.backend.service;

import com.tanm.backend.dto.AuthResponse;
import com.tanm.backend.dto.LoginRequest;
import com.tanm.backend.dto.RegisterRequest;
import com.tanm.backend.dto.UserDto;

public interface AuthService {
    UserDto register(RegisterRequest request);

    /**
     * Verify email address using the registration OTP code.
     */
    void verifyEmail(String email, String otpCode);

    /**
     * Authenticate credentials, verify email status, and return JWT.
     */
    AuthResponse login(LoginRequest request);

    UserDto updateProfile(com.tanm.backend.entity.AppUser user, com.tanm.backend.dto.UserProfileUpdateRequest request);

    void changePassword(com.tanm.backend.entity.AppUser user, com.tanm.backend.dto.ChangePasswordRequest request);

    void devBypassEmailVerification(String email);
}
