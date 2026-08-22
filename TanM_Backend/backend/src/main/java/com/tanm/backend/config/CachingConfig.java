package com.tanm.backend.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@EnableCaching
public class CachingConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(Arrays.asList(
                "dashboardSummary",
                "dashboardRevenue",
                "dashboardOrders",
                "dashboardProducts",
                "dashboardCustomers",
                "dashboardInventory",
                "publicCmsHydration"
        ));
        return cacheManager;
    }
}
