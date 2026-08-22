package com.tanm.backend.service;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsFooterSectionDto;
import com.tanm.backend.dto.ReorderRequest;

import java.util.List;

public interface CmsFooterService {
    List<CmsFooterSectionDto> getActiveFooterSections();
    List<CmsFooterSectionDto> getAllFooterSectionsAdmin();
    CmsFooterSectionDto getFooterSectionById(Long id);
    CmsFooterSectionDto createFooterSection(CmsFooterSectionDto dto);
    CmsFooterSectionDto updateFooterSection(Long id, CmsFooterSectionDto dto);
    void deleteFooterSection(Long id);
    void reorderFooterSections(ReorderRequest request);
    void handleBulkAction(BulkActionRequest request);
}
