package com.tanm.backend.repository;

import com.tanm.backend.entity.Product;
import com.tanm.backend.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.isDeleted = false")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    Optional<Product> findByIdAndIsDeletedFalse(Long id);
    Optional<Product> findBySkuAndIsDeletedFalse(String sku);
    Optional<Product> findBySlugAndIsDeletedFalse(String slug);
    List<Product> findAllByCategoryIdAndIsDeletedFalse(Long categoryId);
    List<Product> findAllByStatusAndIsDeletedFalse(ProductStatus status);
    Page<Product> findByIsDeletedFalse(org.springframework.data.domain.Pageable pageable);
    Page<Product> findByCategoryIdAndIsDeletedFalse(Long categoryId, org.springframework.data.domain.Pageable pageable);
    boolean existsBySku(String sku);
    boolean existsBySlug(String slug);
    boolean existsByCategoryIdAndIsDeletedFalse(Long categoryId);

    @Query("SELECT p FROM Product p " +
           "WHERE p.isDeleted = false AND p.status = com.tanm.backend.enums.ProductStatus.PUBLISHED " +
           "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> searchProductsQuick(@Param("query") String query, org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT p FROM Product p " +
                   "WHERE p.isDeleted = false AND p.status = com.tanm.backend.enums.ProductStatus.PUBLISHED " +
                   "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
                   "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) " +
                   "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))",
           countQuery = "SELECT COUNT(p) FROM Product p " +
                        "WHERE p.isDeleted = false AND p.status = com.tanm.backend.enums.ProductStatus.PUBLISHED " +
                        "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    org.springframework.data.domain.Page<Product> searchProductsPaginated(@Param("query") String query, org.springframework.data.domain.Pageable pageable);
}
