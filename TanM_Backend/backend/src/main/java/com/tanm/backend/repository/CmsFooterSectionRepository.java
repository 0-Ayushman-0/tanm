package com.tanm.backend.repository;

import com.tanm.backend.entity.CmsFooterSection;
import com.tanm.backend.enums.CmsStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsFooterSectionRepository extends JpaRepository<CmsFooterSection, Long> {
    List<CmsFooterSection> findByStatusAndIsDeletedFalseOrderByDisplayOrderAsc(CmsStatus status);
    List<CmsFooterSection> findByIsDeletedFalseOrderByDisplayOrderAsc();
}
