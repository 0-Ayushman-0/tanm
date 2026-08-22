package com.tanm.backend.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsHydrationDto {
    private List<CmsHeroSlideDto> heroSlides;
    private List<CmsSectionConfigDto> sections;
    private CmsAnnouncementBarDto activeAnnouncement;
    private List<CmsNavigationItemDto> navigation;
    private List<CmsFooterSectionDto> footer;
    private Map<String, String> siteSettings;
}
