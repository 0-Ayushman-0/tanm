package com.tanm.backend.repository;

import com.tanm.backend.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    Optional<Collection> findByName(String name);
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    List<Collection> findAllByIsDeletedFalseOrderByDisplayOrderAscIdAsc();
    org.springframework.data.domain.Page<Collection> findByIsDeletedFalse(org.springframework.data.domain.Pageable pageable);
    Optional<Collection> findByIdAndIsDeletedFalse(Long id);
    Optional<Collection> findBySlugAndIsDeletedFalse(String slug);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Collection c " +
           "WHERE c.isDeleted = false AND c.isActive = true " +
           "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Collection> searchCollectionsQuick(@org.springframework.data.repository.query.Param("query") String query, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT c FROM Collection c " +
                   "WHERE c.isDeleted = false AND c.isActive = true " +
                   "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
                   "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')))",
           countQuery = "SELECT COUNT(c) FROM Collection c " +
                        "WHERE c.isDeleted = false AND c.isActive = true " +
                        "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    org.springframework.data.domain.Page<Collection> searchCollectionsPaginated(@org.springframework.data.repository.query.Param("query") String query, org.springframework.data.domain.Pageable pageable);
}
