package com.tanm.backend.repository;

import com.tanm.backend.entity.Product;
import com.tanm.backend.entity.ProductReview;
import com.tanm.backend.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    Page<ProductReview> findByProductAndStatusAndIsDeletedFalse(Product product, ReviewStatus status, Pageable pageable);
    Page<ProductReview> findByStatusAndIsDeletedFalse(ReviewStatus status, Pageable pageable);
    Page<ProductReview> findByIsDeletedFalse(Pageable pageable);

    long countByProductAndStatusAndIsDeletedFalse(Product product, ReviewStatus status);
    long countByProductAndStatusAndRatingAndIsDeletedFalse(Product product, ReviewStatus status, int rating);

    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product = :product AND r.status = :status AND r.isDeleted = false")
    Double getAverageRatingForProduct(Product product, ReviewStatus status);

    Optional<ProductReview> findByIdAndIsDeletedFalse(Long id);
}
