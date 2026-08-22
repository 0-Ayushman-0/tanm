package com.tanm.backend.repository;

import com.tanm.backend.entity.CmsStaticPage;
import com.tanm.backend.enums.CmsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CmsStaticPageRepository extends JpaRepository<CmsStaticPage, Long> {
    Optional<CmsStaticPage> findBySlugAndIsDeletedFalse(String slug);

    @Query("SELECT p FROM CmsStaticPage p " +
           "WHERE p.slug = :slug AND p.isDeleted = false AND p.status = :status " +
           "AND (p.publishAt IS NULL OR p.publishAt <= :now) " +
           "AND (p.unpublishAt IS NULL OR p.unpublishAt > :now)")
    Optional<CmsStaticPage> findPublishedPageBySlug(String slug, CmsStatus status, LocalDateTime now);

    Page<CmsStaticPage> findByIsDeletedFalse(Pageable pageable);
}
