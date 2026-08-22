package com.tanm.backend.controller;

import com.tanm.backend.dto.ProductRatingSummaryDto;
import com.tanm.backend.dto.ProductReviewCreateRequest;
import com.tanm.backend.dto.ProductReviewDto;
import com.tanm.backend.dto.ProductReviewsPageDto;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.service.ProductReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService reviewService;

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ProductReviewsPageDto> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return ResponseEntity.ok(reviewService.getApprovedProductReviews(productId, PageRequest.of(page, size, sort)));
    }

    @GetMapping("/products/{productId}/reviews/summary")
    public ResponseEntity<ProductRatingSummaryDto> getRatingSummary(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductRatingSummary(productId));
    }

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<ProductReviewDto> submitReview(
            @PathVariable Long productId,
            @Valid @RequestBody ProductReviewCreateRequest request,
            @AuthenticationPrincipal AppUser user
    ) {
        return new ResponseEntity<>(reviewService.submitReview(productId, request, user), HttpStatus.CREATED);
    }

    @PostMapping("/reviews/{reviewId}/helpful")
    public ResponseEntity<Map<String, Object>> toggleHelpful(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal AppUser user
    ) {
        boolean voted = reviewService.toggleHelpfulVote(reviewId, user);
        return ResponseEntity.ok(Map.of(
                "voted", voted,
                "message", voted ? "Marked as helpful" : "Helpful vote removed"
        ));
    }
}
