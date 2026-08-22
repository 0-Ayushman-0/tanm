package com.tanm.backend.scheduler;

import com.tanm.backend.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpCleanupScheduler {

    private final OtpTokenRepository otpTokenRepository;

    /**
     * Runs every hour (at the top of the hour) to purge expired or used OTP tokens from the DB.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredOrUsedOtps() {
        log.info("⏰ Starting scheduled OTP cleanup task...");
        try {
            otpTokenRepository.deleteExpiredOrUsed(LocalDateTime.now());
            log.info("✅ Scheduled OTP cleanup task finished successfully.");
        } catch (Exception e) {
            log.error("❌ Failed to complete scheduled OTP cleanup task: {}", e.getMessage());
        }
    }
}
