package com.tanm.backend.repository;

import com.tanm.backend.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findAllByProductIdAndIsDeletedFalseOrderByDisplayOrderAscIdAsc(Long productId);
    Optional<ProductImage> findByIdAndIsDeletedFalse(Long id);
    long countByProductIdAndIsDeletedFalse(Long productId);

    @Modifying
    @Query("UPDATE ProductImage pi SET pi.isPrimary = false WHERE pi.product.id = :productId AND pi.isDeleted = false")
    void resetPrimaryImageForProduct(@Param("productId") Long productId);
}
