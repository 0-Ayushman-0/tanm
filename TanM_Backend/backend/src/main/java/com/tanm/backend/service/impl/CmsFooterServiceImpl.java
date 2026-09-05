package com.tanm.backend.service.impl;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsFooterLinkDto;
import com.tanm.backend.dto.CmsFooterSectionDto;
import com.tanm.backend.dto.ReorderRequest;
import com.tanm.backend.entity.CmsFooterLink;
import com.tanm.backend.entity.CmsFooterSection;
import com.tanm.backend.enums.CmsStatus;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.repository.CmsFooterSectionRepository;
import com.tanm.backend.service.CmsFooterService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CmsFooterServiceImpl implements CmsFooterService {

    private final CmsFooterSectionRepository sectionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CmsFooterSectionDto> getActiveFooterSections() {
        return sectionRepository.findByStatusAndIsDeletedFalseOrderByDisplayOrderAsc(CmsStatus.PUBLISHED).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CmsFooterSectionDto> getAllFooterSectionsAdmin() {
        return sectionRepository.findByIsDeletedFalseOrderByDisplayOrderAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CmsFooterSectionDto getFooterSectionById(Long id) {
        CmsFooterSection section = sectionRepository.findById(id)
                .filter(s -> !s.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Footer section not found with ID: " + id));
        return toDto(section);
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public CmsFooterSectionDto createFooterSection(CmsFooterSectionDto dto) {
        CmsFooterSection section = CmsFooterSection.builder()
                .title(dto.getTitle())
                .displayOrder(dto.getDisplayOrder())
                .status(dto.getStatus() != null ? dto.getStatus() : CmsStatus.PUBLISHED)
                .links(new ArrayList<>())
                .build();

        if (dto.getLinks() != null) {
            for (CmsFooterLinkDto lDto : dto.getLinks()) {
                CmsFooterLink link = CmsFooterLink.builder()
                        .section(section)
                        .label(lDto.getLabel())
                        .url(lDto.getUrl())
                        .displayOrder(lDto.getDisplayOrder())
                        .isExternal(lDto.isExternal())
                        .status(lDto.getStatus() != null ? lDto.getStatus() : CmsStatus.PUBLISHED)
                        .build();
                section.getLinks().add(link);
            }
        }

        return toDto(sectionRepository.save(section));
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public CmsFooterSectionDto updateFooterSection(Long id, CmsFooterSectionDto dto) {
        CmsFooterSection section = sectionRepository.findById(id)
                .filter(s -> !s.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Footer section not found with ID: " + id));

        section.setTitle(dto.getTitle());
        section.setDisplayOrder(dto.getDisplayOrder());
        if (dto.getStatus() != null) section.setStatus(dto.getStatus());

        if (dto.getLinks() != null) {
            section.getLinks().clear();
            for (CmsFooterLinkDto lDto : dto.getLinks()) {
                CmsFooterLink link = CmsFooterLink.builder()
                        .section(section)
                        .label(lDto.getLabel())
                        .url(lDto.getUrl())
                        .displayOrder(lDto.getDisplayOrder())
                        .isExternal(lDto.isExternal())
                        .status(lDto.getStatus() != null ? lDto.getStatus() : CmsStatus.PUBLISHED)
                        .build();
                section.getLinks().add(link);
            }
        }

        return toDto(sectionRepository.save(section));
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void deleteFooterSection(Long id) {
        CmsFooterSection section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Footer section not found with ID: " + id));
        section.setDeleted(true);
        sectionRepository.save(section);
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void reorderFooterSections(ReorderRequest request) {
        List<Long> ids = request.getOrderedIds();
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            int order = i;
            sectionRepository.findById(id).ifPresent(section -> {
                section.setDisplayOrder(order);
                sectionRepository.save(section);
            });
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void handleBulkAction(BulkActionRequest request) {
        for (Long id : request.getIds()) {
            sectionRepository.findById(id).ifPresent(section -> {
                if ("DELETE".equalsIgnoreCase(request.getAction())) {
                    section.setDeleted(true);
                } else if ("STATUS_CHANGE".equalsIgnoreCase(request.getAction()) && request.getStatus() != null) {
                    section.setStatus(request.getStatus());
                }
                sectionRepository.save(section);
            });
        }
    }

    public CmsFooterSectionDto toDto(CmsFooterSection section) {
        if (section == null) return null;

        List<CmsFooterLinkDto> linkDtos = new ArrayList<>();
        if (section.getLinks() != null) {
            linkDtos = section.getLinks().stream()
                    .filter(l -> !l.isDeleted() && l.getStatus() == CmsStatus.PUBLISHED)
                    .map(l -> CmsFooterLinkDto.builder()
                            .id(l.getId())
                            .sectionId(section.getId())
                            .label(l.getLabel())
                            .url(l.getUrl())
                            .displayOrder(l.getDisplayOrder())
                            .isExternal(l.isExternal())
                            .status(l.getStatus())
                            .build())
                    .collect(Collectors.toList());
        }

        return CmsFooterSectionDto.builder()
                .id(section.getId())
                .title(section.getTitle())
                .displayOrder(section.getDisplayOrder())
                .status(section.getStatus())
                .links(linkDtos)
                .build();
    }
}
