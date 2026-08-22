package com.tanm.backend.repository;

import com.tanm.backend.entity.InventoryAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryAuditLogRepository extends JpaRepository<InventoryAuditLog, Long> {
    Page<InventoryAuditLog> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
    Page<InventoryAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
