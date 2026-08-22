package com.tanm.backend.service.impl;

import com.tanm.backend.entity.AppUser;
import com.tanm.backend.enums.OtpPurpose;
import com.tanm.backend.exception.BadRequestException;
import com.tanm.backend.repository.AppUserRepository;
import com.tanm.backend.service.EmailService;
import com.tanm.backend.service.OtpService;
import com.tanm.backend.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final AppUserRepository appUserRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        // Silently bail if user not found (prevent user enumeration attacks)
        if (!appUserRepository.existsByEmailAndIsDeletedFalse(email)) {
            log.info("Password reset requested for non-existent email: {} (silently ignored)", email);
            return;
        }

        // Generate password reset OTP via central OtpService
        String otpCode = otpService.generateOtp(email, OtpPurpose.PASSWORD_RESET);

        // Send OTP email
        emailService.sendPasswordResetOtp(email, otpCode);
        log.info("Password reset OTP issued for: {}", email);
    }

    @Override
    @Transactional
    public void confirmPasswordReset(String email, String otpCode, String newPassword) {
        // Fetch the user first to make sure they exist
        AppUser user = appUserRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new BadRequestException("No account found for this email."));

        // Verify the OTP via central OtpService (this handles hashing checks, expiry, used, attempt count limits)
        otpService.verifyOtp(email, otpCode, OtpPurpose.PASSWORD_RESET);

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(java.time.LocalDateTime.now());
        appUserRepository.save(user);

        // Notify user
        emailService.sendPasswordChangedNotification(email, user.getFirstName());
        log.info("Password reset completed for: {}", email);
    }
}
