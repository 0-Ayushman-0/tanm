package com.tanm.backend.service;

import com.tanm.backend.dto.ProductRatingSummaryDto;
import com.tanm.backend.dto.ProductReviewCreateRequest;
import com.tanm.backend.dto.ProductReviewDto;
import com.tanm.backend.dto.ProductReviewsPageDto;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductReviewService {
    ProductReviewsPageDto getApprovedProductReviews(Long productId, Pageable pageable);
    ProductRatingSummaryDto getProductRatingSummary(Long productId);
    ProductReviewDto submitReview(Long productId, ProductReviewCreateRequest request, AppUser user);
    boolean toggleHelpfulVote(Long reviewId, AppUser user);

    Page<ProductReviewDto> getReviewsAdmin(ReviewStatus status, Pageable pageable);
    ProductReviewDto updateReviewStatus(Long reviewId, ReviewStatus status);
    void deleteReview(Long reviewId);
}
