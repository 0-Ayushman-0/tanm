package com.tanm.backend.repository;

import com.tanm.backend.entity.OtpToken;
import com.tanm.backend.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    /**
     * Fetch all active (unused, not expired) tokens for email + purpose.
     * Used for rate-limit checking.
     */
    @Query("SELECT o FROM OtpToken o WHERE o.email = :email AND o.purpose = :purpose " +
           "AND o.isUsed = false AND o.expiresAt > :since")
    List<OtpToken> findActiveTokens(
            @Param("email") String email,
            @Param("purpose") OtpPurpose purpose,
            @Param("since") LocalDateTime since
    );

    /**
     * Fetch the most recently created active token for matching email+purpose.
     * Used during verification to pick the right token to match against.
     */
    @Query("SELECT o FROM OtpToken o WHERE o.email = :email AND o.purpose = :purpose " +
           "AND o.isUsed = false ORDER BY o.createdAt DESC")
    List<OtpToken> findActiveTokensOrderedByCreatedAt(
            @Param("email") String email,
            @Param("purpose") OtpPurpose purpose
    );

    /**
     * Invalidate all active tokens for an email+purpose before creating a new one.
     */
    @Modifying
    @Query("UPDATE OtpToken o SET o.isUsed = true WHERE o.email = :email " +
           "AND o.purpose = :purpose AND o.isUsed = false")
    void invalidateAllActive(
            @Param("email") String email,
            @Param("purpose") OtpPurpose purpose
    );

    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.expiresAt < :now OR o.isUsed = true")
    void deleteExpiredOrUsed(@Param("now") LocalDateTime now);
}
