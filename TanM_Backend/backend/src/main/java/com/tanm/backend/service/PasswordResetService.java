package com.tanm.backend.service;

public interface PasswordResetService {

    /**
     * Generate and email a password reset OTP for the given email address.
     * Silently does nothing if the user does not exist (to prevent user enumeration).
     */
    void requestPasswordReset(String email);

    /**
     * Verify the OTP, and if valid, update the user's password to the provided new password.
     */
    void confirmPasswordReset(String email, String otpCode, String newPassword);
}
