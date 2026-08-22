package com.tanm.backend.service.impl;

import com.tanm.backend.dto.*;
import com.tanm.backend.entity.*;
import com.tanm.backend.enums.ReviewStatus;
import com.tanm.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class ProductReviewServiceImplTest {

    @Mock
    private ProductReviewRepository reviewRepository;

    @Mock
    private ReviewHelpfulVoteRepository helpfulVoteRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MediaRepository mediaRepository;

    @InjectMocks
    private ProductReviewServiceImpl reviewService;

    private Product product;
    private AppUser user;

    @BeforeEach
    void setUp() {
        product = Product.builder().name("Leather Duffle Bag").sku("BAG-001").build();
        product.setId(5L);

        user = AppUser.builder().email("buyer@tanm.com").firstName("Rahul").build();
        user.setId(12L);
    }

    @Test
    void submitReview_shouldSetVerifiedPurchaseTrueWhenUserHasPaidOrder() {
        Mockito.when(productRepository.findByIdAndIsDeletedFalse(5L))
                .thenReturn(Optional.of(product));
        Mockito.when(orderRepository.existsVerifiedPurchase(user, 5L))
                .thenReturn(true);
        Mockito.when(reviewRepository.save(any(ProductReview.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductReviewCreateRequest req = ProductReviewCreateRequest.builder()
                .rating(5)
                .title("Excellent Craftsmanship!")
                .comment("Top notch full grain leather.")
                .build();

        ProductReviewDto result = reviewService.submitReview(5L, req, user);

        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.isVerifiedPurchase()).isTrue();
        assertThat(result.getStatus()).isEqualTo(ReviewStatus.PENDING_MODERATION);
    }

    @Test
    void toggleHelpfulVote_shouldAddVoteAndIncrementCount() {
        ProductReview review = ProductReview.builder()
                .product(product)
                .user(user)
                .rating(5)
                .comment("Great quality")
                .helpfulCount(3)
                .build();
        review.setId(20L);

        Mockito.when(reviewRepository.findByIdAndIsDeletedFalse(20L))
                .thenReturn(Optional.of(review));
        Mockito.when(helpfulVoteRepository.findByReviewAndUser(review, user))
                .thenReturn(Optional.empty());

        boolean voted = reviewService.toggleHelpfulVote(20L, user);

        assertThat(voted).isTrue();
        assertThat(review.getHelpfulCount()).isEqualTo(4);
        Mockito.verify(helpfulVoteRepository).save(any(ReviewHelpfulVote.class));
    }

    @Test
    void getProductRatingSummary_shouldReturnCalculatedBreakdown() {
        Mockito.when(productRepository.findByIdAndIsDeletedFalse(5L))
                .thenReturn(Optional.of(product));
        Mockito.when(reviewRepository.countByProductAndStatusAndIsDeletedFalse(product, ReviewStatus.APPROVED))
                .thenReturn(10L);
        Mockito.when(reviewRepository.getAverageRatingForProduct(product, ReviewStatus.APPROVED))
                .thenReturn(4.6);
        Mockito.when(reviewRepository.countByProductAndStatusAndRatingAndIsDeletedFalse(product, ReviewStatus.APPROVED, 5))
                .thenReturn(7L);

        ProductRatingSummaryDto summary = reviewService.getProductRatingSummary(5L);

        assertThat(summary.getTotalReviews()).isEqualTo(10);
        assertThat(summary.getAverageRating()).isEqualTo(4.6);
        assertThat(summary.getStar5Count()).isEqualTo(7);
    }
}
