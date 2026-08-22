package com.tanm.backend.service;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsAnnouncementBarDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CmsAnnouncementService {
    CmsAnnouncementBarDto getActiveAnnouncement();
    Page<CmsAnnouncementBarDto> getAllAnnouncementsAdmin(Pageable pageable);
    CmsAnnouncementBarDto getAnnouncementById(Long id);
    CmsAnnouncementBarDto createAnnouncement(CmsAnnouncementBarDto dto);
    CmsAnnouncementBarDto updateAnnouncement(Long id, CmsAnnouncementBarDto dto);
    void deleteAnnouncement(Long id);
    void handleBulkAction(BulkActionRequest request);
}
