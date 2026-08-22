package com.tanm.backend.repository;

import com.tanm.backend.entity.CmsNavigationItem;
import com.tanm.backend.enums.CmsStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsNavigationItemRepository extends JpaRepository<CmsNavigationItem, Long> {
    List<CmsNavigationItem> findByParentIsNullAndStatusAndIsDeletedFalseOrderByDisplayOrderAsc(CmsStatus status);
    List<CmsNavigationItem> findByParentIsNullAndIsDeletedFalseOrderByDisplayOrderAsc();
}
