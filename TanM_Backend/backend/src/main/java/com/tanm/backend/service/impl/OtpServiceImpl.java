package com.tanm.backend.service.impl;

import com.tanm.backend.entity.OtpToken;
import com.tanm.backend.enums.OtpPurpose;
import com.tanm.backend.exception.BadRequestException;
import com.tanm.backend.repository.OtpTokenRepository;
import com.tanm.backend.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // Max wrong guesses before token is locked out
    private static final int MAX_ATTEMPTS = 5;

    // Rate limit: max OTP requests per window
    private static final int MAX_REQUESTS_PER_WINDOW = 3;

    @Value("${app.otp.password-reset.expiry-minutes:15}")
    private int passwordResetExpiryMinutes;

    @Value("${app.otp.login.expiry-minutes:10}")
    private int loginExpiryMinutes;

    @Value("${app.otp.email-verification.expiry-minutes:30}")
    private int emailVerificationExpiryMinutes;

    @Value("${app.otp.rate-limit.window-minutes:15}")
    private int rateLimitWindowMinutes;

    // ========================================================
    // Generate OTP
    // ========================================================

    @Override
    @Transactional
    public String generateOtp(String email, OtpPurpose purpose) {
        // 1. Rate limit check
        enforceRateLimit(email, purpose);

        // 2. Invalidate all previous active tokens for this email+purpose
        otpTokenRepository.invalidateAllActive(email, purpose);

        // 3. Generate 6-digit raw OTP
        String rawOtp = String.valueOf(100000 + SECURE_RANDOM.nextInt(900000));

        // 4. Hash and persist — never store plaintext
        String otpHash = passwordEncoder.encode(rawOtp);

        OtpToken token = OtpToken.builder()
                .email(email)
                .otpHash(otpHash)
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutesFor(purpose)))
                .attemptCount(0)
                .isUsed(false)
                .build();

        otpTokenRepository.save(token);
        log.info("OTP generated for [{}] purpose=[{}]", email, purpose);

        return rawOtp; // returned to be emailed, never stored as-is
    }

    // ========================================================
    // Verify OTP
    // ========================================================

    @Override
    @Transactional
    public void verifyOtp(String email, String rawOtp, OtpPurpose purpose) {
        List<OtpToken> candidates = otpTokenRepository
                .findActiveTokensOrderedByCreatedAt(email, purpose);

        if (candidates.isEmpty()) {
            throw new BadRequestException("No active OTP found. Please request a new one.");
        }

        OtpToken token = candidates.get(0); // most recent active token

        // Check expiry
        if (token.isExpired()) {
            token.setUsed(true);
            otpTokenRepository.save(token);
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        // Check attempt limit
        if (token.getAttemptCount() >= MAX_ATTEMPTS) {
            throw new BadRequestException(
                    "Too many incorrect attempts. Please request a new OTP.");
        }

        // Match against stored hash
        if (!passwordEncoder.matches(rawOtp, token.getOtpHash())) {
            token.setAttemptCount(token.getAttemptCount() + 1);
            otpTokenRepository.save(token);

            int remaining = MAX_ATTEMPTS - token.getAttemptCount();
            if (remaining <= 0) {
                throw new BadRequestException(
                        "Too many incorrect attempts. Please request a new OTP.");
            }
            throw new BadRequestException(
                    "Invalid OTP. " + remaining + " attempt(s) remaining.");
        }

        // Success — mark as used
        token.setUsed(true);
        token.setUsedAt(LocalDateTime.now());
        otpTokenRepository.save(token);
        log.info("OTP verified successfully for [{}] purpose=[{}]", email, purpose);
    }

    // ========================================================
    // Helpers
    // ========================================================

    private void enforceRateLimit(String email, OtpPurpose purpose) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(rateLimitWindowMinutes);
        List<OtpToken> recentTokens = otpTokenRepository
                .findActiveTokens(email, purpose, windowStart);

        if (recentTokens.size() >= MAX_REQUESTS_PER_WINDOW) {
            log.warn("Rate limit exceeded for [{}] purpose=[{}]", email, purpose);
            throw new BadRequestException(
                    "Too many OTP requests. Please wait " + rateLimitWindowMinutes +
                    " minutes before requesting again.");
        }
    }

    private int expiryMinutesFor(OtpPurpose purpose) {
        return switch (purpose) {
            case PASSWORD_RESET -> passwordResetExpiryMinutes;
            case LOGIN_2FA -> loginExpiryMinutes;
            case EMAIL_VERIFICATION -> emailVerificationExpiryMinutes;
        };
    }
}
