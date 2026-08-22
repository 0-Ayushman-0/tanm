package com.tanm.backend.repository;

import com.tanm.backend.entity.CmsAnnouncementBar;
import com.tanm.backend.enums.CmsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CmsAnnouncementBarRepository extends JpaRepository<CmsAnnouncementBar, Long> {

    @Query("SELECT a FROM CmsAnnouncementBar a " +
           "WHERE a.isDeleted = false AND a.status = :status " +
           "AND (a.startDate IS NULL OR a.startDate <= :now) " +
           "AND (a.endDate IS NULL OR a.endDate > :now) " +
           "ORDER BY a.updatedAt DESC")
    List<CmsAnnouncementBar> findActiveAnnouncements(CmsStatus status, LocalDateTime now);

    Page<CmsAnnouncementBar> findByIsDeletedFalse(Pageable pageable);
}
