package com.tanm.backend.service;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsNavigationItemDto;
import com.tanm.backend.dto.ReorderRequest;

import java.util.List;

public interface CmsNavigationService {
    List<CmsNavigationItemDto> getActiveNavigationTree();
    List<CmsNavigationItemDto> getAllNavigationItemsAdmin();
    CmsNavigationItemDto getNavigationItemById(Long id);
    CmsNavigationItemDto createNavigationItem(CmsNavigationItemDto dto);
    CmsNavigationItemDto updateNavigationItem(Long id, CmsNavigationItemDto dto);
    void deleteNavigationItem(Long id);
    void reorderNavigationItems(ReorderRequest request);
    void handleBulkAction(BulkActionRequest request);
}
