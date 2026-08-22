package com.tanm.backend.service;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsPageVersionDto;
import com.tanm.backend.dto.CmsStaticPageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CmsPageService {
    CmsStaticPageDto getPublishedPageBySlug(String slug);
    Page<CmsStaticPageDto> getAllPagesAdmin(Pageable pageable);
    CmsStaticPageDto getPageById(Long id);
    CmsStaticPageDto createPage(CmsStaticPageDto dto);
    CmsStaticPageDto updatePage(Long id, CmsStaticPageDto dto);
    CmsStaticPageDto publishPage(Long id, String publishedBy);
    CmsStaticPageDto rollbackToVersion(Long pageId, int versionNumber, String requestedBy);
    Page<CmsPageVersionDto> getPageVersions(Long pageId, Pageable pageable);
    void deletePage(Long id);
    void handleBulkAction(BulkActionRequest request);
}
