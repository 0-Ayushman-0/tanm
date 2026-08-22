package com.tanm.backend.controller;

import com.tanm.backend.dto.*;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.mapper.UserMapper;
import com.tanm.backend.service.AuthService;
import com.tanm.backend.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        UserDto registered = authService.register(request);
        return new ResponseEntity<>(registered, HttpStatus.CREATED);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.getEmail(), request.getOtpCode());
        return ResponseEntity.ok(Map.of("message", "Email verified successfully. Your account is now active."));
    }

    @PostMapping("/dev-bypass")
    public ResponseEntity<Map<String, String>> devBypass(@RequestParam String email) {
        authService.devBypassEmailVerification(email);
        return ResponseEntity.ok(Map.of("message", "Email verification bypassed for " + email));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal AppUser user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    // ========================================================
    // Password Reset
    // ========================================================

    /**
     * Step 1 – Request a password reset OTP.
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<Map<String, String>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDto request) {
        passwordResetService.requestPasswordReset(request.getEmail());
        // Always return 200 even if email not found (prevent user enumeration)
        return ResponseEntity.ok(Map.of("message", "If that email is registered, you will receive an OTP shortly."));
    }

    /**
     * Step 2 – Confirm OTP and set new password.
     */
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Map<String, String>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmDto request) {
        passwordResetService.confirmPasswordReset(
                request.getEmail(), request.getOtpCode(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password changed successfully. Please log in."));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(
            @AuthenticationPrincipal AppUser user,
            @Valid @RequestBody com.tanm.backend.dto.UserProfileUpdateRequest request) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDto updated = authService.updateProfile(user, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal AppUser user,
            @Valid @RequestBody com.tanm.backend.dto.ChangePasswordRequest request) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        authService.changePassword(user, request);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
    }
}
