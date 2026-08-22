package com.tanm.backend.repository;

import com.tanm.backend.entity.CmsFooterLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CmsFooterLinkRepository extends JpaRepository<CmsFooterLink, Long> {
}
