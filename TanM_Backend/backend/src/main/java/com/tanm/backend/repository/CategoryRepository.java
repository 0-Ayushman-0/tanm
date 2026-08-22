package com.tanm.backend.repository;

import com.tanm.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    java.util.List<Category> findAllByIsDeletedFalseOrderByDisplayOrderAscIdAsc();
    org.springframework.data.domain.Page<Category> findByIsDeletedFalse(org.springframework.data.domain.Pageable pageable);
    Optional<Category> findByIdAndIsDeletedFalse(Long id);
    Optional<Category> findBySlugAndIsDeletedFalse(String slug);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Category c " +
           "WHERE c.isDeleted = false AND c.isActive = true " +
           "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    java.util.List<Category> searchCategoriesQuick(@org.springframework.data.repository.query.Param("query") String query, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT c FROM Category c " +
                   "WHERE c.isDeleted = false AND c.isActive = true " +
                   "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
                   "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')))",
           countQuery = "SELECT COUNT(c) FROM Category c " +
                        "WHERE c.isDeleted = false AND c.isActive = true " +
                        "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    org.springframework.data.domain.Page<Category> searchCategoriesPaginated(@org.springframework.data.repository.query.Param("query") String query, org.springframework.data.domain.Pageable pageable);
}
