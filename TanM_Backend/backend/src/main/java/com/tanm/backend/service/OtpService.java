package com.tanm.backend.service;

import com.tanm.backend.enums.OtpPurpose;

public interface OtpService {

    /**
     * Generate a new OTP for the given email+purpose, store it hashed,
     * invalidate all previous active tokens, and return the raw OTP code
     * (to be emailed — never persisted in plaintext).
     *
     * @throws com.tanm.backend.exception.BadRequestException if rate limit exceeded
     */
    String generateOtp(String email, OtpPurpose purpose);

    /**
     * Verify the submitted raw OTP code against the stored hash.
     * Increments attemptCount on failure.
     * Marks token as used on success.
     *
     * @throws com.tanm.backend.exception.BadRequestException on invalid/expired/exceeded OTP
     */
    void verifyOtp(String email, String rawOtp, OtpPurpose purpose);
}
