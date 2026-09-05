package com.tanm.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanm.backend.dto.CmsStaticPageDto;
import com.tanm.backend.entity.CmsPageVersion;
import com.tanm.backend.entity.CmsStaticPage;
import com.tanm.backend.enums.CmsStatus;
import com.tanm.backend.repository.CmsPageVersionRepository;
import com.tanm.backend.repository.CmsStaticPageRepository;
import com.tanm.backend.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class CmsPageServiceImplTest {

    @Mock
    private CmsStaticPageRepository pageRepository;

    @Mock
    private CmsPageVersionRepository versionRepository;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CmsPageServiceImpl pageService;

    private CmsStaticPage page;

    @BeforeEach
    void setUp() {
        page = CmsStaticPage.builder()
                .title("About Us")
                .slug("about-us")
                .content("<p>Initial Content</p>")
                .status(CmsStatus.DRAFT)
                .currentVersionNumber(1)
                .build();
        page.setId(10L);
    }

    @Test
    void createPage_shouldCreateDraftPage() {
        Mockito.when(pageRepository.findBySlugAndIsDeletedFalse("about-us"))
                .thenReturn(Optional.empty());
        Mockito.when(pageRepository.save(any(CmsStaticPage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CmsStaticPageDto dto = CmsStaticPageDto.builder()
                .title("About Us")
                .slug("about-us")
                .content("<p>Initial Content</p>")
                .build();

        CmsStaticPageDto created = pageService.createPage(dto);

        assertThat(created.getTitle()).isEqualTo("About Us");
        assertThat(created.getSlug()).isEqualTo("about-us");
        assertThat(created.getStatus()).isEqualTo(CmsStatus.DRAFT);
    }

    @Test
    void publishPage_shouldSaveVersionSnapshotAndSetStatusToPublished() {
        Mockito.when(pageRepository.findById(10L))
                .thenReturn(Optional.of(page));
        Mockito.when(pageRepository.save(any(CmsStaticPage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CmsStaticPageDto published = pageService.publishPage(10L, "admin@tanm.com");

        assertThat(published.getStatus()).isEqualTo(CmsStatus.PUBLISHED);
        assertThat(published.getCurrentVersionNumber()).isEqualTo(2);

        Mockito.verify(versionRepository).save(any(CmsPageVersion.class));
    }

    @Test
    void rollbackToVersion_shouldRestoreTitleAndContent() {
        CmsPageVersion v1 = CmsPageVersion.builder()
                .page(page)
                .versionNumber(1)
                .title("Original About Us Title")
                .content("<p>Original Content</p>")
                .publishedAt(LocalDateTime.now().minusDays(2))
                .build();

        Mockito.when(pageRepository.findById(10L))
                .thenReturn(Optional.of(page));
        Mockito.when(versionRepository.findByPageAndVersionNumber(page, 1))
                .thenReturn(Optional.of(v1));
        Mockito.when(pageRepository.save(any(CmsStaticPage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CmsStaticPageDto restored = pageService.rollbackToVersion(10L, 1, "admin@tanm.com");

        assertThat(restored.getTitle()).isEqualTo("Original About Us Title");
        assertThat(restored.getContent()).isEqualTo("<p>Original Content</p>");
        assertThat(restored.getStatus()).isEqualTo(CmsStatus.DRAFT);
    }
}
