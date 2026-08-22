package com.tanm.backend.service.impl;

import com.tanm.backend.entity.OtpToken;
import com.tanm.backend.enums.OtpPurpose;
import com.tanm.backend.exception.BadRequestException;
import com.tanm.backend.repository.OtpTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock
    private OtpTokenRepository otpTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OtpServiceImpl otpService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(otpService, "passwordResetExpiryMinutes", 15);
        ReflectionTestUtils.setField(otpService, "loginExpiryMinutes", 10);
        ReflectionTestUtils.setField(otpService, "emailVerificationExpiryMinutes", 30);
        ReflectionTestUtils.setField(otpService, "rateLimitWindowMinutes", 15);
    }

    @Test
    void generateOtp_shouldCreateHashAndInvalidateActiveTokens() {
        // Arrange
        String email = "test@example.com";
        OtpPurpose purpose = OtpPurpose.EMAIL_VERIFICATION;
        Mockito.when(otpTokenRepository.findActiveTokens(eq(email), eq(purpose), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        Mockito.when(passwordEncoder.encode(any(String.class))).thenReturn("hashed_otp");

        // Act
        String rawOtp = otpService.generateOtp(email, purpose);

        // Assert
        assertNotNull(rawOtp);
        assertEquals(6, rawOtp.length());
        Mockito.verify(otpTokenRepository).invalidateAllActive(email, purpose);
        Mockito.verify(otpTokenRepository).save(any(OtpToken.class));
    }

    @Test
    void generateOtp_shouldThrowRateLimitExceeded() {
        // Arrange
        String email = "test@example.com";
        OtpPurpose purpose = OtpPurpose.EMAIL_VERIFICATION;
        List<OtpToken> activeTokens = List.of(
                new OtpToken(), new OtpToken(), new OtpToken()
        );
        Mockito.when(otpTokenRepository.findActiveTokens(eq(email), eq(purpose), any(LocalDateTime.class)))
                .thenReturn(activeTokens);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> otpService.generateOtp(email, purpose));
    }

    @Test
    void verifyOtp_shouldMarkUsedOnSuccessfulMatch() {
        // Arrange
        String email = "test@example.com";
        String rawOtp = "123456";
        OtpPurpose purpose = OtpPurpose.EMAIL_VERIFICATION;
        OtpToken token = OtpToken.builder()
                .email(email)
                .otpHash("hashed_otp")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .isUsed(false)
                .attemptCount(0)
                .build();

        Mockito.when(otpTokenRepository.findActiveTokensOrderedByCreatedAt(email, purpose))
                .thenReturn(List.of(token));
        Mockito.when(passwordEncoder.matches(rawOtp, "hashed_otp")).thenReturn(true);

        // Act
        otpService.verifyOtp(email, rawOtp, purpose);

        // Assert
        assertTrue(token.isUsed());
        assertNotNull(token.getUsedAt());
        Mockito.verify(otpTokenRepository).save(token);
    }

    @Test
    void verifyOtp_shouldIncrementAttemptsOnFailedMatch() {
        // Arrange
        String email = "test@example.com";
        String rawOtp = "123456";
        OtpPurpose purpose = OtpPurpose.EMAIL_VERIFICATION;
        OtpToken token = OtpToken.builder()
                .email(email)
                .otpHash("hashed_otp")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .isUsed(false)
                .attemptCount(0)
                .build();

        Mockito.when(otpTokenRepository.findActiveTokensOrderedByCreatedAt(email, purpose))
                .thenReturn(List.of(token));
        Mockito.when(passwordEncoder.matches(rawOtp, "hashed_otp")).thenReturn(false);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> otpService.verifyOtp(email, rawOtp, purpose));
        assertTrue(exception.getMessage().contains("Invalid OTP"));
        assertEquals(1, token.getAttemptCount());
        Mockito.verify(otpTokenRepository).save(token);
    }

    @Test
    void verifyOtp_shouldBlockAfterMaxAttempts() {
        // Arrange
        String email = "test@example.com";
        String rawOtp = "123456";
        OtpPurpose purpose = OtpPurpose.EMAIL_VERIFICATION;
        OtpToken token = OtpToken.builder()
                .email(email)
                .otpHash("hashed_otp")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .isUsed(false)
                .attemptCount(5)
                .build();

        Mockito.when(otpTokenRepository.findActiveTokensOrderedByCreatedAt(email, purpose))
                .thenReturn(List.of(token));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> otpService.verifyOtp(email, rawOtp, purpose));
        assertTrue(exception.getMessage().contains("Too many incorrect attempts"));
    }
}
