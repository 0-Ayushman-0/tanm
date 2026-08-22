package com.tanm.backend.repository;

import com.tanm.backend.entity.AppUser;
import com.tanm.backend.entity.ProductReview;
import com.tanm.backend.entity.ReviewHelpfulVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewHelpfulVoteRepository extends JpaRepository<ReviewHelpfulVote, Long> {
    Optional<ReviewHelpfulVote> findByReviewAndUser(ProductReview review, AppUser user);
    boolean existsByReviewAndUser(ProductReview review, AppUser user);
}
