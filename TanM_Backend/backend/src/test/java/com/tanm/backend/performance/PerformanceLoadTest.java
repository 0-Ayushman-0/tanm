package com.tanm.backend.performance;

import com.tanm.backend.entity.Category;
import com.tanm.backend.entity.Product;
import com.tanm.backend.enums.ProductStatus;
import com.tanm.backend.repository.CategoryRepository;
import com.tanm.backend.repository.ProductRepository;
import com.tanm.backend.service.AdminDashboardService;
import com.tanm.backend.service.CmsHydrationService;
import com.tanm.backend.service.ProductReviewService;
import com.tanm.backend.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
public class PerformanceLoadTest {

    private static final Logger log = LoggerFactory.getLogger(PerformanceLoadTest.class);

    @Autowired
    private CmsHydrationService hydrationService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private ProductReviewService reviewService;

    @Autowired
    private AdminDashboardService dashboardService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long testProductId;

    @BeforeEach
    void seedTestData() {
        Category category = categoryRepository.findByName("Bags")
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name("Bags")
                        .slug("bags-" + System.currentTimeMillis())
                        .description("Leather bags")
                        .build()));

        Product product = productRepository.findBySkuAndIsDeletedFalse("PERF-BAG-001")
                .orElseGet(() -> productRepository.save(Product.builder()
                        .name("Executive Leather Duffle Bag")
                        .sku("PERF-BAG-001")
                        .slug("executive-leather-duffle-bag-" + System.currentTimeMillis())
                        .shortDescription("Premium travel bag")
                        .description("Handcrafted from full-grain Italian leather.")
                        .price(BigDecimal.valueOf(14999.00))
                        .stockQuantity(100)
                        .status(ProductStatus.PUBLISHED)
                        .category(category)
                        .build()));

        testProductId = product.getId();
    }

    @Test
    @DisplayName("🔥 High-Concurrency Load Test: 100+ Parallel Browsing, Search, CMS & Analytics Users")
    void runHighConcurrencyStressTest() throws InterruptedException {
        int totalThreads = 100;
        int requestsPerThread = 5;
        int totalRequests = totalThreads * requestsPerThread;

        ExecutorService executorService = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(totalThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Long> latencies = Collections.synchronizedList(new ArrayList<>());

        log.info("🚀 Starting High-Concurrency Stress Test with [{}] threads (Total requests: [{}])...", totalThreads, totalRequests);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalThreads; i++) {
            final int workerId = i;
            executorService.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    for (int req = 0; req < requestsPerThread; req++) {
                        long reqStart = System.nanoTime();
                        try {
                            // Rotate workloads simulating realistic customer & admin browsing behavior
                            switch ((workerId + req) % 5) {
                                case 0:
                                    // Scenario 1: CMS Hydration Endpoint
                                    hydrationService.getHydrationPayload();
                                    break;
                                case 1:
                                    // Scenario 2: Global Search (Product, SKU, Collection, Category)
                                    searchService.globalSearch("leather", 5);
                                    break;
                                case 2:
                                    // Scenario 3: Product Rating Summary & Reviews
                                    reviewService.getProductRatingSummary(testProductId);
                                    break;
                                case 3:
                                    // Scenario 4: Admin Analytical Dashboard Summary
                                    dashboardService.getSummary();
                                    break;
                                case 4:
                                    // Scenario 5: Domain Product Search
                                    searchService.searchProducts("Executive", org.springframework.data.domain.PageRequest.of(0, 10));
                                    break;
                            }
                            long reqDurationMs = (System.nanoTime() - reqStart) / 1_000_000;
                            latencies.add(reqDurationMs);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                            log.error("Worker [{}] request failed", workerId, e);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Fire all threads simultaneously!
        startLatch.countDown();

        boolean completed = finishLatch.await(60, TimeUnit.SECONDS);
        long totalDurationMs = System.currentTimeMillis() - startTime;
        executorService.shutdown();

        assertThat(completed).isTrue();
        assertThat(failureCount.get()).isEqualTo(0);
        assertThat(successCount.get()).isEqualTo(totalRequests);

        // Compute Benchmark Statistics
        double throughputRps = (double) successCount.get() / (totalDurationMs / 1000.0);
        double avgLatencyMs = latencies.stream().mapToLong(Long::longValue).average().orElse(0.0);

        List<Long> sortedLatencies = new ArrayList<>(latencies);
        Collections.sort(sortedLatencies);
        long p95LatencyMs = sortedLatencies.isEmpty() ? 0 : sortedLatencies.get((int) (sortedLatencies.size() * 0.95));

        log.info("==========================================================");
        log.info("📊 PERFORMANCE BENCHMARK REPORT");
        log.info("==========================================================");
        log.info("Total Concurrent Threads : {}", totalThreads);
        log.info("Total Requests Handled   : {}", successCount.get());
        log.info("Total Test Duration      : {} ms", totalDurationMs);
        log.info("Throughput (Ops/sec)     : {} req/sec", String.format("%.2f", throughputRps));
        log.info("Average Latency          : {} ms", String.format("%.2f", avgLatencyMs));
        log.info("P95 Latency              : {} ms", p95LatencyMs);
        log.info("Success Rate             : {}%", (successCount.get() * 100.0) / totalRequests);
        log.info("Failures / Deadlocks     : {}", failureCount.get());
        log.info("==========================================================");
    }
}
