package com.tanm.backend.entity;

import com.tanm.backend.common.BaseEntity;
import com.tanm.backend.enums.OtpPurpose;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_tokens", indexes = {
        @Index(name = "idx_otp_email_purpose", columnList = "email, purpose"),
        @Index(name = "idx_otp_email_used", columnList = "email, is_used")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpToken extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String email;

    /**
     * Stored as BCrypt hash — never plaintext.
     */
    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OtpPurpose purpose;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Timestamp of when the OTP was successfully used. Null if not yet used.
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * Number of failed verification attempts against this token.
     */
    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private int attemptCount = 0;

    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private boolean isUsed = false;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
