package com.tanm.backend.service.impl;

import com.tanm.backend.config.JwtService;
import com.tanm.backend.dto.AuthResponse;
import com.tanm.backend.dto.LoginRequest;
import com.tanm.backend.dto.RegisterRequest;
import com.tanm.backend.dto.UserDto;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.enums.OtpPurpose;
import com.tanm.backend.enums.UserRole;
import com.tanm.backend.exception.BadRequestException;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.mapper.UserMapper;
import com.tanm.backend.repository.AppUserRepository;
import com.tanm.backend.service.AuthService;
import com.tanm.backend.service.EmailService;
import com.tanm.backend.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final OtpService otpService;

    @Override
    @Transactional
    public UserDto register(RegisterRequest request) {
        if (appUserRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        AppUser user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.CUSTOMER); // Hardcode CUSTOMER role for registration
        user.setEmailVerified(false);    // Must verify email before logging in

        AppUser saved = appUserRepository.save(user);

        // Generate email verification OTP and send via email
        String otpCode = otpService.generateOtp(saved.getEmail(), OtpPurpose.EMAIL_VERIFICATION);
        emailService.sendEmailVerificationOtp(saved.getEmail(), otpCode);

        log.info("User registered successfully. Verification email sent to [{}]", saved.getEmail());
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void verifyEmail(String email, String otpCode) {
        AppUser user = appUserRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new BadRequestException("No account found for this email."));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email address is already verified.");
        }

        // Verify the OTP via central OtpService
        otpService.verifyOtp(email, otpCode, OtpPurpose.EMAIL_VERIFICATION);

        // Activate user
        user.setEmailVerified(true);
        appUserRepository.save(user);

        // Send welcome email upon successful verification
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
        log.info("Email verified successfully. Account activated for [{}]", email);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        // Enforce email verification check
        if (!user.isEmailVerified()) {
            throw new BadRequestException("Please verify your email address before logging in.");
        }

        // Issue JWT token directly
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userMapper.toDto(user))
                .build();
    }

    @Override
    @Transactional
    public UserDto updateProfile(AppUser user, com.tanm.backend.dto.UserProfileUpdateRequest request) {
        AppUser managedUser = appUserRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        managedUser.setFirstName(request.getFirstName());
        if (request.getLastName() != null) {
            managedUser.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            managedUser.setPhoneNumber(request.getPhoneNumber());
        }

        AppUser updated = appUserRepository.save(managedUser);
        return userMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void changePassword(AppUser user, com.tanm.backend.dto.ChangePasswordRequest request) {
        AppUser managedUser = appUserRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), managedUser.getPassword())) {
            throw new BadRequestException("Current password does not match");
        }

        managedUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        appUserRepository.save(managedUser);
        log.info("User [{}] changed their password", managedUser.getEmail());
    }

    @Override
    @Transactional
    public void devBypassEmailVerification(String email) {
        AppUser user = appUserRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new BadRequestException("No account found for email: " + email));
        user.setEmailVerified(true);
        appUserRepository.save(user);
        log.info("Dev Mode: Bypassed email verification for [{}]", email);
    }
}
