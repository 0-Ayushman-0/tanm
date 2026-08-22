package com.tanm.backend.repository;

import com.tanm.backend.entity.CmsSectionConfig;
import com.tanm.backend.enums.CmsStatus;
import com.tanm.backend.enums.SectionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CmsSectionConfigRepository extends JpaRepository<CmsSectionConfig, Long> {
    Optional<CmsSectionConfig> findBySectionTypeAndIsDeletedFalse(SectionType sectionType);
    List<CmsSectionConfig> findByStatusAndIsDeletedFalseOrderByDisplayOrderAsc(CmsStatus status);
    List<CmsSectionConfig> findByIsDeletedFalseOrderByDisplayOrderAsc();
}
