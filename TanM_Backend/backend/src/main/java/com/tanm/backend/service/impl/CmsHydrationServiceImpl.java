package com.tanm.backend.service.impl;

import com.tanm.backend.dto.*;
import com.tanm.backend.enums.CmsStatus;
import com.tanm.backend.repository.CmsSectionConfigRepository;
import com.tanm.backend.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmsHydrationServiceImpl implements CmsHydrationService {

    private final CmsHeroService heroService;
    private final CmsSectionConfigRepository sectionConfigRepository;
    private final CmsAnnouncementService announcementService;
    private final CmsNavigationService navigationService;
    private final CmsFooterService footerService;
    private final CmsSiteSettingService siteSettingService;

    @Override
    @Cacheable(value = "publicCmsHydration")
    @Transactional(readOnly = true)
    public CmsHydrationDto getHydrationPayload() {
        log.info("🌐 Hydrating public CMS payload (Cache Miss)...");

        List<CmsHeroSlideDto> heroSlides = heroService.getActiveHeroSlides();
        
        List<CmsSectionConfigDto> sections = sectionConfigRepository
                .findByStatusAndIsDeletedFalseOrderByDisplayOrderAsc(CmsStatus.PUBLISHED).stream()
                .map(s -> CmsSectionConfigDto.builder()
                        .id(s.getId())
                        .sectionType(s.getSectionType())
                        .title(s.getTitle())
                        .subtitle(s.getSubtitle())
                        .displayOrder(s.getDisplayOrder())
                        .status(s.getStatus())
                        .configurationJson(s.getConfigurationJson())
                        .build())
                .collect(Collectors.toList());

        CmsAnnouncementBarDto announcement = announcementService.getActiveAnnouncement();
        List<CmsNavigationItemDto> navigation = navigationService.getActiveNavigationTree();
        List<CmsFooterSectionDto> footer = footerService.getActiveFooterSections();
        var settings = siteSettingService.getAllSettingsAsMap();

        return CmsHydrationDto.builder()
                .heroSlides(heroSlides)
                .sections(sections)
                .activeAnnouncement(announcement)
                .navigation(navigation)
                .footer(footer)
                .siteSettings(settings)
                .build();
    }
}
