package com.tanm.backend.repository;

import com.tanm.backend.entity.CmsBanner;
import com.tanm.backend.enums.BannerType;
import com.tanm.backend.enums.CmsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CmsBannerRepository extends JpaRepository<CmsBanner, Long> {

    @Query("SELECT b FROM CmsBanner b " +
           "WHERE b.isDeleted = false AND b.bannerType = :type AND b.status = :status " +
           "AND (b.startDate IS NULL OR b.startDate <= :now) " +
           "AND (b.endDate IS NULL OR b.endDate > :now) " +
           "ORDER BY b.priority DESC")
    List<CmsBanner> findActiveBanners(BannerType type, CmsStatus status, LocalDateTime now);

    Page<CmsBanner> findByIsDeletedFalse(Pageable pageable);
}
