package com.tanm.backend.service.impl;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsAnnouncementBarDto;
import com.tanm.backend.entity.CmsAnnouncementBar;
import com.tanm.backend.enums.CmsStatus;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.repository.CmsAnnouncementBarRepository;
import com.tanm.backend.service.CmsAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CmsAnnouncementServiceImpl implements CmsAnnouncementService {

    private final CmsAnnouncementBarRepository announcementBarRepository;

    @Override
    @Transactional(readOnly = true)
    public CmsAnnouncementBarDto getActiveAnnouncement() {
        List<CmsAnnouncementBar> activeList = announcementBarRepository.findActiveAnnouncements(CmsStatus.PUBLISHED, LocalDateTime.now());
        if (activeList.isEmpty()) {
            return null;
        }
        return toDto(activeList.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CmsAnnouncementBarDto> getAllAnnouncementsAdmin(Pageable pageable) {
        return announcementBarRepository.findByIsDeletedFalse(pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CmsAnnouncementBarDto getAnnouncementById(Long id) {
        CmsAnnouncementBar bar = announcementBarRepository.findById(id)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Announcement bar not found with ID: " + id));
        return toDto(bar);
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public CmsAnnouncementBarDto createAnnouncement(CmsAnnouncementBarDto dto) {
        CmsAnnouncementBar bar = CmsAnnouncementBar.builder()
                .text(dto.getText())
                .linkUrl(dto.getLinkUrl())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .backgroundColor(dto.getBackgroundColor() != null ? dto.getBackgroundColor() : "#000000")
                .textColor(dto.getTextColor() != null ? dto.getTextColor() : "#FFFFFF")
                .status(dto.getStatus() != null ? dto.getStatus() : CmsStatus.DRAFT)
                .build();
        return toDto(announcementBarRepository.save(bar));
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public CmsAnnouncementBarDto updateAnnouncement(Long id, CmsAnnouncementBarDto dto) {
        CmsAnnouncementBar bar = announcementBarRepository.findById(id)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Announcement bar not found with ID: " + id));

        bar.setText(dto.getText());
        bar.setLinkUrl(dto.getLinkUrl());
        bar.setStartDate(dto.getStartDate());
        bar.setEndDate(dto.getEndDate());
        if (dto.getBackgroundColor() != null) bar.setBackgroundColor(dto.getBackgroundColor());
        if (dto.getTextColor() != null) bar.setTextColor(dto.getTextColor());
        if (dto.getStatus() != null) bar.setStatus(dto.getStatus());

        return toDto(announcementBarRepository.save(bar));
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void deleteAnnouncement(Long id) {
        CmsAnnouncementBar bar = announcementBarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement bar not found with ID: " + id));
        bar.setDeleted(true);
        announcementBarRepository.save(bar);
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void handleBulkAction(BulkActionRequest request) {
        for (Long id : request.getIds()) {
            announcementBarRepository.findById(id).ifPresent(bar -> {
                if ("DELETE".equalsIgnoreCase(request.getAction())) {
                    bar.setDeleted(true);
                } else if ("STATUS_CHANGE".equalsIgnoreCase(request.getAction()) && request.getStatus() != null) {
                    bar.setStatus(request.getStatus());
                }
                announcementBarRepository.save(bar);
            });
        }
    }

    public CmsAnnouncementBarDto toDto(CmsAnnouncementBar bar) {
        if (bar == null) return null;
        return CmsAnnouncementBarDto.builder()
                .id(bar.getId())
                .text(bar.getText())
                .linkUrl(bar.getLinkUrl())
                .startDate(bar.getStartDate())
                .endDate(bar.getEndDate())
                .backgroundColor(bar.getBackgroundColor())
                .textColor(bar.getTextColor())
                .status(bar.getStatus())
                .build();
    }
}
