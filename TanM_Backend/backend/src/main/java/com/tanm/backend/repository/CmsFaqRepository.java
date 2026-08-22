package com.tanm.backend.repository;

import com.tanm.backend.entity.CmsFaq;
import com.tanm.backend.enums.CmsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsFaqRepository extends JpaRepository<CmsFaq, Long> {
    List<CmsFaq> findByStatusAndIsDeletedFalseOrderByDisplayOrderAsc(CmsStatus status);
    Page<CmsFaq> findByIsDeletedFalse(Pageable pageable);
}
