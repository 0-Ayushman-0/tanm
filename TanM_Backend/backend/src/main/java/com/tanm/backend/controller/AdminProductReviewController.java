package com.tanm.backend.controller;

import com.tanm.backend.dto.ProductReviewDto;
import com.tanm.backend.enums.ReviewStatus;
import com.tanm.backend.service.ProductReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminProductReviewController {

    private final ProductReviewService reviewService;

    @GetMapping
    public ResponseEntity<Page<ProductReviewDto>> getAll(
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return ResponseEntity.ok(reviewService.getReviewsAdmin(status, PageRequest.of(page, size, sort)));
    }

    @PatchMapping("/{reviewId}/status")
    public ResponseEntity<ProductReviewDto> updateStatus(
            @PathVariable Long reviewId,
            @RequestParam ReviewStatus status
    ) {
        return ResponseEntity.ok(reviewService.updateReviewStatus(reviewId, status));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
