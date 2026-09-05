package com.tanm.backend.repository;

import com.tanm.backend.entity.CmsAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CmsAuditLogRepository extends JpaRepository<CmsAuditLog, Long> {
    Page<CmsAuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);
    Page<CmsAuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
}
