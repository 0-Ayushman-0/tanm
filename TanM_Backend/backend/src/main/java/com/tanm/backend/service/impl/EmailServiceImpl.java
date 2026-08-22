package com.tanm.backend.service.impl;

import com.tanm.backend.dto.OrderDto;
import com.tanm.backend.dto.OrderItemDto;
import com.tanm.backend.entity.EmailLog;
import com.tanm.backend.enums.EmailTemplate;
import com.tanm.backend.repository.EmailLogRepository;
import com.tanm.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.bcc:er.amishra08@gmail.com}")
    private String bccEmail;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    // ========================================================
    // Public API
    // ========================================================

    @Async
    @Override
    public void sendWelcomeEmail(String toEmail, String firstName) {
        String subject = "Welcome to TanM – Your premium leather goods store 🎉";
        String body = buildWelcomeHtml(firstName);
        send(toEmail, subject, body, EmailTemplate.WELCOME);
    }

    @Async
    @Override
    public void sendLoginOtp(String toEmail, String otpCode) {
        String subject = "Your TanM Login OTP Code";
        String body = buildOtpHtml(
                "Your Login OTP",
                "Use the code below to complete your login. This code is valid for <strong>10 minutes</strong>.",
                otpCode,
                "If you did not try to log in, please secure your account immediately."
        );
        send(toEmail, subject, body, EmailTemplate.LOGIN_OTP);
    }

    @Async
    @Override
    public void sendPasswordResetOtp(String toEmail, String otpCode) {
        String subject = "Reset your TanM password";
        String body = buildOtpHtml(
                "Password Reset OTP",
                "Use the code below to reset your password. This code is valid for <strong>15 minutes</strong>.",
                otpCode,
                "If you did not request a password reset, you can safely ignore this email."
        );
        send(toEmail, subject, body, EmailTemplate.PASSWORD_RESET);
    }

    @Async
    @Override
    public void sendEmailVerificationOtp(String toEmail, String otpCode) {
        String subject = "Verify your TanM account";
        String body = buildOtpHtml(
                "Verify your Email",
                "Use the code below to verify your email address and activate your account. This code is valid for <strong>30 minutes</strong>.",
                otpCode,
                "If you did not register for a TanM account, please ignore this email."
        );
        send(toEmail, subject, body, EmailTemplate.EMAIL_VERIFICATION);
    }

    @Async
    @Override
    public void sendPasswordChangedNotification(String toEmail, String firstName) {
        String subject = "Your TanM password has been changed";
        String body = buildSimpleNotificationHtml(
                "Password Changed Successfully",
                "Hi " + firstName + ",",
                "Your TanM account password was successfully changed. If this was not you, please contact support immediately.",
                "Contact Support",
                "mailto:support@tanm.store"
        );
        send(toEmail, subject, body, EmailTemplate.PASSWORD_CHANGED);
    }

    @Async
    @Override
    public void sendOrderConfirmation(OrderDto order) {
        String subject = "Order Confirmed – " + order.getOrderNumber();
        String body = buildOrderConfirmationHtml(order);
        send(order.getUserEmail(), subject, body, EmailTemplate.ORDER_CONFIRMATION);
    }

    @Async
    @Override
    public void sendShippingUpdate(OrderDto order) {
        String subject = "Your TanM Order is on its way! 📦 – " + order.getOrderNumber();
        String body = buildShippingUpdateHtml(order);
        send(order.getUserEmail(), subject, body, EmailTemplate.SHIPPING);
    }

    // ========================================================
    // Internal send logic - falls back to console log in dev
    // ========================================================

    private void send(String to, String subject, String htmlBody, EmailTemplate template) {
        if (!mailEnabled || fromEmail == null || fromEmail.isBlank()) {
            logToConsole(to, subject, htmlBody);
            auditLog(to, subject, template, "SENT_CONSOLE", null);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            if (bccEmail != null && !bccEmail.isBlank()) {
                helper.setBcc(bccEmail);
            }
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("📧 Email sent to [{}]: {}", to, subject);
            auditLog(to, subject, template, "SENT", null);
        } catch (Exception e) {
            log.error("❌ Failed to send email to [{}]: {}", to, e.getMessage());
            logToConsole(to, subject, htmlBody);
            auditLog(to, subject, template, "FAILED", e.getMessage());
        }
    }

    private void auditLog(String to, String subject, EmailTemplate template, String status, String failureReason) {
        try {
            EmailLog emailLog = EmailLog.builder()
                    .recipient(to)
                    .subject(subject)
                    .template(template)
                    .status(status)
                    .sentAt(LocalDateTime.now())
                    .failureReason(failureReason != null && failureReason.length() > 990 
                            ? failureReason.substring(0, 990) 
                            : failureReason)
                    .build();
            emailLogRepository.save(emailLog);
        } catch (Exception ex) {
            log.error("Failed to write email audit log to database: {}", ex.getMessage());
        }
    }

    private void logToConsole(String to, String subject, String htmlBody) {
        log.info("""
                \n
                ╔══════════════════════════════════════════════════════╗
                ║              📧 TanM Email (Console Mode)            ║
                ╠══════════════════════════════════════════════════════╣
                ║  To      : {}
                ║  Subject : {}
                ╠══════════════════════════════════════════════════════╣
                {}
                ╚══════════════════════════════════════════════════════╝
                """, to, subject, htmlBody);
    }

    // ========================================================
    // HTML Builders
    // ========================================================

    private String buildWelcomeHtml(String firstName) {
        return wrapInLayout("""
                <h1 style="color:#1a1a1a;">Welcome to TanM, %s! 🎉</h1>
                <p style="color:#555;font-size:16px;">
                    We're thrilled to have you join our community of premium leather goods enthusiasts.
                </p>
                <p style="color:#555;font-size:16px;">
                    Explore our curated collection of handcrafted wallets, bags, and accessories – built to last a lifetime.
                </p>
                <a href="http://localhost:3000/shop"
                   style="display:inline-block;margin-top:20px;padding:14px 32px;background:#1a1a1a;color:#fff;
                          text-decoration:none;border-radius:6px;font-size:16px;font-weight:600;">
                    Browse Collection
                </a>
                """.formatted(firstName));
    }

    private String buildOtpHtml(String title, String description, String otpCode, String disclaimer) {
        return wrapInLayout("""
                <h1 style="color:#1a1a1a;">%s</h1>
                <p style="color:#555;font-size:16px;">%s</p>
                <div style="margin:30px 0;text-align:center;">
                    <div style="display:inline-block;padding:20px 40px;background:#f4f4f4;
                                border-radius:12px;border:2px dashed #1a1a1a;">
                        <span style="font-size:42px;font-weight:700;letter-spacing:16px;
                                     color:#1a1a1a;font-family:monospace;">%s</span>
                    </div>
                </div>
                <p style="color:#999;font-size:14px;margin-top:20px;">%s</p>
                """.formatted(title, description, otpCode, disclaimer));
    }

    private String buildSimpleNotificationHtml(String title, String greeting, String body, String ctaText, String ctaLink) {
        return wrapInLayout("""
                <h1 style="color:#1a1a1a;">%s</h1>
                <p style="color:#555;font-size:16px;">%s</p>
                <p style="color:#555;font-size:16px;">%s</p>
                <a href="%s"
                   style="display:inline-block;margin-top:20px;padding:14px 32px;background:#1a1a1a;color:#fff;
                          text-decoration:none;border-radius:6px;font-size:16px;font-weight:600;">
                    %s
                </a>
                """.formatted(title, greeting, body, ctaLink, ctaText));
    }

    private String buildOrderConfirmationHtml(OrderDto order) {
        String itemsHtml = order.getItems().stream()
                .map(item -> """
                        <tr>
                            <td style="padding:10px 0;border-bottom:1px solid #eee;color:#333;">%s</td>
                            <td style="padding:10px 0;border-bottom:1px solid #eee;color:#333;text-align:center;">%d</td>
                            <td style="padding:10px 0;border-bottom:1px solid #eee;color:#333;text-align:right;">₹%s</td>
                        </tr>
                        """.formatted(item.getProductName(), item.getQuantity(), item.getSubtotal()))
                .collect(Collectors.joining());

        return wrapInLayout("""
                <h1 style="color:#1a1a1a;">Order Confirmed! ✅</h1>
                <p style="color:#555;">Thank you for your purchase. Your order <strong>%s</strong> has been confirmed and is being prepared.</p>
                <table style="width:100%%;border-collapse:collapse;margin:20px 0;">
                    <thead>
                        <tr style="background:#f8f8f8;">
                            <th style="padding:10px;text-align:left;color:#666;">Product</th>
                            <th style="padding:10px;text-align:center;color:#666;">Qty</th>
                            <th style="padding:10px;text-align:right;color:#666;">Subtotal</th>
                        </tr>
                    </thead>
                    <tbody>%s</tbody>
                </table>
                <div style="text-align:right;margin-top:10px;">
                    <p style="color:#555;">Subtotal: <strong>₹%s</strong></p>
                    <p style="color:#555;">Shipping: <strong>₹%s</strong></p>
                    <p style="font-size:18px;color:#1a1a1a;">Total: <strong>₹%s</strong></p>
                </div>
                <div style="background:#f8f8f8;padding:16px;border-radius:8px;margin-top:20px;">
                    <p style="color:#555;margin:0;"><strong>Shipping to:</strong><br/>
                    %s, %s, %s - %s</p>
                </div>
                """.formatted(
                order.getOrderNumber(),
                itemsHtml,
                order.getSubtotal(),
                order.getShippingFee(),
                order.getGrandTotal(),
                order.getShippingFullName(),
                order.getShippingCity(),
                order.getShippingState(),
                order.getShippingPostalCode()
        ));
    }

    private String buildShippingUpdateHtml(OrderDto order) {
        return wrapInLayout("""
                <h1 style="color:#1a1a1a;">Your order is on its way! 📦</h1>
                <p style="color:#555;font-size:16px;">
                    Order <strong>%s</strong> has been shipped and is heading your way.
                </p>
                %s
                <div style="background:#f8f8f8;padding:16px;border-radius:8px;margin-top:20px;">
                    <p style="color:#555;margin:0;"><strong>Delivering to:</strong><br/>
                    %s, %s, %s - %s</p>
                </div>
                """.formatted(
                order.getOrderNumber(),
                (order.getTrackingNumber() != null ? """
                        <div style="background:#fff8e1;border-left:4px solid #f59e0b;padding:14px 18px;border-radius:4px;margin:20px 0;">
                            <p style="margin:0;color:#666;">Tracking Number</p>
                            <p style="margin:4px 0 0;font-size:22px;font-weight:700;color:#1a1a1a;letter-spacing:2px;">%s</p>
                            %s
                        </div>
                        """.formatted(
                        order.getTrackingNumber(),
                        order.getCarrier() != null ? "<p style=\"color:#888;margin:4px 0 0;\">Carrier: " + order.getCarrier() + "</p>" : ""
                ) : ""),
                order.getShippingFullName(),
                order.getShippingCity(),
                order.getShippingState(),
                order.getShippingPostalCode()
        ));
    }

    private String wrapInLayout(String content) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"></head>
                <body style="margin:0;padding:0;background:#f0f0f0;font-family:'Segoe UI',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f0f0f0;padding:40px 20px;">
                    <tr><td align="center">
                      <table width="600" cellpadding="0" cellspacing="0"
                             style="background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                        <!-- Header -->
                        <tr>
                          <td style="background:#1a1a1a;padding:28px 40px;text-align:center;">
                            <span style="color:#fff;font-size:28px;font-weight:700;letter-spacing:4px;">TanM</span>
                            <span style="display:block;color:#aaa;font-size:12px;letter-spacing:2px;margin-top:4px;">PREMIUM LEATHER GOODS</span>
                          </td>
                        </tr>
                        <!-- Body -->
                        <tr><td style="padding:40px;">%s</td></tr>
                        <!-- Footer -->
                        <tr>
                          <td style="background:#f8f8f8;padding:20px 40px;text-align:center;border-top:1px solid #eee;">
                            <p style="color:#aaa;font-size:12px;margin:0;">© 2026 TanM. All rights reserved.</p>
                            <p style="color:#aaa;font-size:12px;margin:4px 0 0;">You're receiving this because you have an account at TanM.</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(content);
    }
}
