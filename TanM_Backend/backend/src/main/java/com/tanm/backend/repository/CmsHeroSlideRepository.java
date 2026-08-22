package com.tanm.backend.repository;

import com.tanm.backend.entity.CmsHeroSlide;
import com.tanm.backend.enums.CmsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CmsHeroSlideRepository extends JpaRepository<CmsHeroSlide, Long> {

    @Query("SELECT h FROM CmsHeroSlide h " +
           "WHERE h.isDeleted = false AND h.status = :status " +
           "AND (h.publishAt IS NULL OR h.publishAt <= :now) " +
           "AND (h.unpublishAt IS NULL OR h.unpublishAt > :now) " +
           "ORDER BY h.sortOrder ASC")
    List<CmsHeroSlide> findActiveHeroSlides(CmsStatus status, LocalDateTime now);

    Page<CmsHeroSlide> findByIsDeletedFalse(Pageable pageable);
}
