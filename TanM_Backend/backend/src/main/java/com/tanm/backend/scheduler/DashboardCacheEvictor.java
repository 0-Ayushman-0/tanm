package com.tanm.backend.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardCacheEvictor {

    private final CacheManager cacheManager;

    /**
     * Evict all dashboard statistics caches every 30 seconds to refresh metrics.
     */
    @Scheduled(fixedRate = 30000)
    public void evictDashboardCaches() {
        log.debug("🧹 Evicting all dashboard analytics caches...");
        cacheManager.getCacheNames().forEach(cacheName -> 
                Objects.requireNonNull(cacheManager.getCache(cacheName)).clear()
        );
    }
}
