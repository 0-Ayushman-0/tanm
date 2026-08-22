package com.tanm.backend.service.impl;

import com.tanm.backend.dto.*;
import com.tanm.backend.entity.*;
import com.tanm.backend.enums.ReviewStatus;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.repository.*;
import com.tanm.backend.service.ProductReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ReviewHelpfulVoteRepository helpfulVoteRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final MediaRepository mediaRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductReviewsPageDto getApprovedProductReviews(Long productId, Pageable pageable) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        ProductRatingSummaryDto summary = getProductRatingSummary(productId);
        Page<ProductReviewDto> reviews = reviewRepository
                .findByProductAndStatusAndIsDeletedFalse(product, ReviewStatus.APPROVED, pageable)
                .map(this::toDto);

        return ProductReviewsPageDto.builder()
                .summary(summary)
                .reviews(reviews)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductRatingSummaryDto getProductRatingSummary(Long productId) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        long total = reviewRepository.countByProductAndStatusAndIsDeletedFalse(product, ReviewStatus.APPROVED);
        Double avg = reviewRepository.getAverageRatingForProduct(product, ReviewStatus.APPROVED);

        long star5 = reviewRepository.countByProductAndStatusAndRatingAndIsDeletedFalse(product, ReviewStatus.APPROVED, 5);
        long star4 = reviewRepository.countByProductAndStatusAndRatingAndIsDeletedFalse(product, ReviewStatus.APPROVED, 4);
        long star3 = reviewRepository.countByProductAndStatusAndRatingAndIsDeletedFalse(product, ReviewStatus.APPROVED, 3);
        long star2 = reviewRepository.countByProductAndStatusAndRatingAndIsDeletedFalse(product, ReviewStatus.APPROVED, 2);
        long star1 = reviewRepository.countByProductAndStatusAndRatingAndIsDeletedFalse(product, ReviewStatus.APPROVED, 1);

        return ProductRatingSummaryDto.builder()
                .productId(productId)
                .averageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0)
                .totalReviews(total)
                .star5Count(star5)
                .star4Count(star4)
                .star3Count(star3)
                .star2Count(star2)
                .star1Count(star1)
                .build();
    }

    @Override
    @Transactional
    public ProductReviewDto submitReview(Long productId, ProductReviewCreateRequest request, AppUser user) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        boolean isVerified = orderRepository.existsVerifiedPurchase(user, productId);

        List<Media> images = new ArrayList<>();
        if (request.getImageMediaIds() != null && !request.getImageMediaIds().isEmpty()) {
            images = mediaRepository.findAllById(request.getImageMediaIds());
        }

        ProductReview review = ProductReview.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .isVerifiedPurchase(isVerified)
                .status(ReviewStatus.PENDING_MODERATION) // Default moderation state
                .helpfulCount(0)
                .images(images)
                .build();

        log.info("User [{}] submitted review for product [{}], verified purchase: [{}]", user.getEmail(), product.getSku(), isVerified);
        return toDto(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public boolean toggleHelpfulVote(Long reviewId, AppUser user) {
        ProductReview review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        Optional<ReviewHelpfulVote> existing = helpfulVoteRepository.findByReviewAndUser(review, user);
        if (existing.isPresent()) {
            helpfulVoteRepository.delete(existing.get());
            review.setHelpfulCount(Math.max(0, review.getHelpfulCount() - 1));
            reviewRepository.save(review);
            return false; // Vote removed
        } else {
            ReviewHelpfulVote vote = ReviewHelpfulVote.builder()
                    .review(review)
                    .user(user)
                    .build();
            helpfulVoteRepository.save(vote);
            review.setHelpfulCount(review.getHelpfulCount() + 1);
            reviewRepository.save(review);
            return true; // Vote added
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductReviewDto> getReviewsAdmin(ReviewStatus status, Pageable pageable) {
        Page<ProductReview> page = (status != null)
                ? reviewRepository.findByStatusAndIsDeletedFalse(status, pageable)
                : reviewRepository.findByIsDeletedFalse(pageable);
        return page.map(this::toDto);
    }

    @Override
    @Transactional
    public ProductReviewDto updateReviewStatus(Long reviewId, ReviewStatus status) {
        ProductReview review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        review.setStatus(status);
        log.info("Admin updated review [{}] status to [{}]", reviewId, status);
        return toDto(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
        review.setDeleted(true);
        reviewRepository.save(review);
    }

    public ProductReviewDto toDto(ProductReview review) {
        if (review == null) return null;

        List<MediaDto> imageDtos = new ArrayList<>();
        if (review.getImages() != null) {
            imageDtos = review.getImages().stream()
                    .filter(m -> !m.isDeleted())
                    .map(m -> MediaDto.builder()
                            .id(m.getId())
                            .url(m.getUrl())
                            .thumbnailUrl(m.getThumbnailUrl())
                            .altText(m.getAltText())
                            .build())
                    .collect(Collectors.toList());
        }

        return ProductReviewDto.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFirstName() != null ? review.getUser().getFirstName() : "Customer")
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .isVerifiedPurchase(review.isVerifiedPurchase())
                .status(review.getStatus())
                .helpfulCount(review.getHelpfulCount())
                .images(imageDtos)
                .createdAt(review.getCreatedAt())
                .build();
    }
}
