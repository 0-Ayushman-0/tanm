package com.tanm.backend.repository;

import com.tanm.backend.entity.CmsPageVersion;
import com.tanm.backend.entity.CmsStaticPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CmsPageVersionRepository extends JpaRepository<CmsPageVersion, Long> {
    Page<CmsPageVersion> findByPageOrderByVersionNumberDesc(CmsStaticPage page, Pageable pageable);
    Optional<CmsPageVersion> findByPageAndVersionNumber(CmsStaticPage page, int versionNumber);
}
