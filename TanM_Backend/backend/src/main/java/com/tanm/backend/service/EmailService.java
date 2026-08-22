package com.tanm.backend.service;

import com.tanm.backend.dto.OrderDto;

public interface EmailService {

    void sendWelcomeEmail(String toEmail, String firstName);

    void sendLoginOtp(String toEmail, String otpCode);

    void sendPasswordResetOtp(String toEmail, String otpCode);

    void sendEmailVerificationOtp(String toEmail, String otpCode);

    void sendPasswordChangedNotification(String toEmail, String firstName);

    void sendOrderConfirmation(OrderDto order);

    void sendShippingUpdate(OrderDto order);
}
