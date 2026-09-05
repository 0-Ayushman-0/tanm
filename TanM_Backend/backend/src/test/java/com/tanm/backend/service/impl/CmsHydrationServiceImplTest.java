package com.tanm.backend.service.impl;

import com.tanm.backend.dto.*;
import com.tanm.backend.entity.CmsSectionConfig;
import com.tanm.backend.enums.CmsStatus;
import com.tanm.backend.enums.SectionType;
import com.tanm.backend.repository.CmsSectionConfigRepository;
import com.tanm.backend.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CmsHydrationServiceImplTest {

    @Mock
    private CmsHeroService heroService;

    @Mock
    private CmsSectionConfigRepository sectionConfigRepository;

    @Mock
    private CmsAnnouncementService announcementService;

    @Mock
    private CmsNavigationService navigationService;

    @Mock
    private CmsFooterService footerService;

    @Mock
    private CmsSiteSettingService siteSettingService;

    @InjectMocks
    private CmsHydrationServiceImpl hydrationService;

    @Test
    void getHydrationPayload_shouldAssembleAllActiveCmsElements() {
        CmsHeroSlideDto hero = CmsHeroSlideDto.builder().title("Welcome Slide").build();
        CmsSectionConfig section = CmsSectionConfig.builder()
                .sectionType(SectionType.FEATURED_PRODUCTS)
                .title("New Arrivals")
                .displayOrder(1)
                .status(CmsStatus.PUBLISHED)
                .build();
        CmsAnnouncementBarDto announcement = CmsAnnouncementBarDto.builder().text("Free Shipping on orders above ₹150").build();
        CmsNavigationItemDto nav = CmsNavigationItemDto.builder().label("Shop").url("/products").build();
        CmsFooterSectionDto footer = CmsFooterSectionDto.builder().title("Customer Support").build();

        Mockito.when(heroService.getActiveHeroSlides()).thenReturn(List.of(hero));
        Mockito.when(sectionConfigRepository.findByStatusAndIsDeletedFalseOrderByDisplayOrderAsc(CmsStatus.PUBLISHED))
                .thenReturn(List.of(section));
        Mockito.when(announcementService.getActiveAnnouncement()).thenReturn(announcement);
        Mockito.when(navigationService.getActiveNavigationTree()).thenReturn(List.of(nav));
        Mockito.when(footerService.getActiveFooterSections()).thenReturn(List.of(footer));
        Mockito.when(siteSettingService.getAllSettingsAsMap()).thenReturn(Map.of("store_name", "TanM Leather"));

        CmsHydrationDto payload = hydrationService.getHydrationPayload();

        assertThat(payload.getHeroSlides()).hasSize(1);
        assertThat(payload.getHeroSlides().get(0).getTitle()).isEqualTo("Welcome Slide");
        assertThat(payload.getSections()).hasSize(1);
        assertThat(payload.getSections().get(0).getSectionType()).isEqualTo(SectionType.FEATURED_PRODUCTS);
        assertThat(payload.getActiveAnnouncement().getText()).contains("Free Shipping");
        assertThat(payload.getNavigation()).hasSize(1);
        assertThat(payload.getFooter()).hasSize(1);
        assertThat(payload.getSiteSettings()).containsEntry("store_name", "TanM Leather");
    }
}
