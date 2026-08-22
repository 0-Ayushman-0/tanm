package com.tanm.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanm.backend.dto.*;
import com.tanm.backend.entity.*;
import com.tanm.backend.enums.CmsStatus;
import com.tanm.backend.exception.BadRequestException;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.repository.*;
import com.tanm.backend.service.CmsPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmsPageServiceImpl implements CmsPageService {

    private final CmsStaticPageRepository pageRepository;
    private final CmsPageVersionRepository versionRepository;
    private final MediaRepository mediaRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public CmsStaticPageDto getPublishedPageBySlug(String slug) {
        CmsStaticPage page = pageRepository.findPublishedPageBySlug(slug, CmsStatus.PUBLISHED, LocalDateTime.now())
                .orElseThrow(() -> new ResourceNotFoundException("Published page not found with slug: " + slug));
        return toDto(page);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CmsStaticPageDto> getAllPagesAdmin(Pageable pageable) {
        return pageRepository.findByIsDeletedFalse(pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CmsStaticPageDto getPageById(Long id) {
        CmsStaticPage page = pageRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with ID: " + id));
        return toDto(page);
    }

    @Override
    @Transactional
    public CmsStaticPageDto createPage(CmsStaticPageDto dto) {
        pageRepository.findBySlugAndIsDeletedFalse(dto.getSlug())
                .ifPresent(existing -> {
                    throw new BadRequestException("A page with slug '" + dto.getSlug() + "' already exists.");
                });

        SeoMetadata seo = buildSeoMetadata(dto.getSeoMetadata());

        CmsStaticPage page = CmsStaticPage.builder()
                .title(dto.getTitle())
                .slug(dto.getSlug().toLowerCase().trim().replaceAll("\\s+", "-"))
                .content(dto.getContent())
                .seoMetadata(seo)
                .status(dto.getStatus() != null ? dto.getStatus() : CmsStatus.DRAFT)
                .publishAt(dto.getPublishAt())
                .unpublishAt(dto.getUnpublishAt())
                .currentVersionNumber(1)
                .build();

        return toDto(pageRepository.save(page));
    }

    @Override
    @Transactional
    public CmsStaticPageDto updatePage(Long id, CmsStaticPageDto dto) {
        CmsStaticPage page = pageRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with ID: " + id));

        if (!page.getSlug().equalsIgnoreCase(dto.getSlug())) {
            pageRepository.findBySlugAndIsDeletedFalse(dto.getSlug())
                    .ifPresent(existing -> {
                        throw new BadRequestException("A page with slug '" + dto.getSlug() + "' already exists.");
                    });
            page.setSlug(dto.getSlug().toLowerCase().trim().replaceAll("\\s+", "-"));
        }

        page.setTitle(dto.getTitle());
        page.setContent(dto.getContent());
        page.setPublishAt(dto.getPublishAt());
        page.setUnpublishAt(dto.getUnpublishAt());
        if (dto.getStatus() != null) page.setStatus(dto.getStatus());

        if (dto.getSeoMetadata() != null) {
            if (page.getSeoMetadata() == null) {
                page.setSeoMetadata(buildSeoMetadata(dto.getSeoMetadata()));
            } else {
                updateSeoMetadata(page.getSeoMetadata(), dto.getSeoMetadata());
            }
        }

        return toDto(pageRepository.save(page));
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public CmsStaticPageDto publishPage(Long id, String publishedBy) {
        CmsStaticPage page = pageRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with ID: " + id));

        page.setStatus(CmsStatus.PUBLISHED);
        page.setPublishedAt(LocalDateTime.now());
        int nextVersion = page.getCurrentVersionNumber() + 1;
        page.setCurrentVersionNumber(nextVersion);

        String seoJson = null;
        try {
            if (page.getSeoMetadata() != null) {
                seoJson = objectMapper.writeValueAsString(page.getSeoMetadata());
            }
        } catch (Exception e) {
            log.warn("Could not serialize SEO metadata for page versioning", e);
        }

        CmsPageVersion version = CmsPageVersion.builder()
                .page(page)
                .versionNumber(nextVersion)
                .title(page.getTitle())
                .content(page.getContent())
                .seoMetadataJson(seoJson)
                .publishedBy(publishedBy != null ? publishedBy : "ADMIN")
                .publishedAt(LocalDateTime.now())
                .build();

        versionRepository.save(version);
        return toDto(pageRepository.save(page));
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public CmsStaticPageDto rollbackToVersion(Long pageId, int versionNumber, String requestedBy) {
        CmsStaticPage page = pageRepository.findById(pageId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with ID: " + pageId));

        CmsPageVersion version = versionRepository.findByPageAndVersionNumber(page, versionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Page version " + versionNumber + " not found."));

        page.setTitle(version.getTitle());
        page.setContent(version.getContent());
        page.setStatus(CmsStatus.DRAFT); // Replaced state starts as DRAFT for review

        log.info("Rolled back page [{}] to version [{}] by user [{}]", page.getSlug(), versionNumber, requestedBy);
        return toDto(pageRepository.save(page));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CmsPageVersionDto> getPageVersions(Long pageId, Pageable pageable) {
        CmsStaticPage page = pageRepository.findById(pageId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with ID: " + pageId));

        return versionRepository.findByPageOrderByVersionNumberDesc(page, pageable)
                .map(v -> CmsPageVersionDto.builder()
                        .id(v.getId())
                        .pageId(page.getId())
                        .versionNumber(v.getVersionNumber())
                        .title(v.getTitle())
                        .content(v.getContent())
                        .seoMetadataJson(v.getSeoMetadataJson())
                        .publishedBy(v.getPublishedBy())
                        .publishedAt(v.getPublishedAt())
                        .build());
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void deletePage(Long id) {
        CmsStaticPage page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with ID: " + id));
        page.setDeleted(true);
        pageRepository.save(page);
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void handleBulkAction(BulkActionRequest request) {
        for (Long id : request.getIds()) {
            pageRepository.findById(id).ifPresent(page -> {
                if ("DELETE".equalsIgnoreCase(request.getAction())) {
                    page.setDeleted(true);
                } else if ("STATUS_CHANGE".equalsIgnoreCase(request.getAction()) && request.getStatus() != null) {
                    page.setStatus(request.getStatus());
                }
                pageRepository.save(page);
            });
        }
    }

    private SeoMetadata buildSeoMetadata(SeoMetadataDto dto) {
        if (dto == null) return null;
        Media ogImg = dto.getOgImageId() != null ? mediaRepository.findById(dto.getOgImageId()).orElse(null) : null;
        return SeoMetadata.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .keywords(dto.getKeywords())
                .canonicalUrl(dto.getCanonicalUrl())
                .ogTitle(dto.getOgTitle())
                .ogDescription(dto.getOgDescription())
                .ogImage(ogImg)
                .robots(dto.getRobots() != null ? dto.getRobots() : "index, follow")
                .build();
    }

    private void updateSeoMetadata(SeoMetadata target, SeoMetadataDto dto) {
        target.setTitle(dto.getTitle());
        target.setDescription(dto.getDescription());
        target.setKeywords(dto.getKeywords());
        target.setCanonicalUrl(dto.getCanonicalUrl());
        target.setOgTitle(dto.getOgTitle());
        target.setOgDescription(dto.getOgDescription());
        if (dto.getRobots() != null) target.setRobots(dto.getRobots());
        if (dto.getOgImageId() != null) {
            target.setOgImage(mediaRepository.findById(dto.getOgImageId()).orElse(null));
        }
    }

    public CmsStaticPageDto toDto(CmsStaticPage page) {
        if (page == null) return null;
        SeoMetadataDto seoDto = null;
        if (page.getSeoMetadata() != null) {
            SeoMetadata s = page.getSeoMetadata();
            seoDto = SeoMetadataDto.builder()
                    .id(s.getId())
                    .title(s.getTitle())
                    .description(s.getDescription())
                    .keywords(s.getKeywords())
                    .canonicalUrl(s.getCanonicalUrl())
                    .ogTitle(s.getOgTitle())
                    .ogDescription(s.getOgDescription())
                    .robots(s.getRobots())
                    .ogImageId(s.getOgImage() != null ? s.getOgImage().getId() : null)
                    .build();
        }

        return CmsStaticPageDto.builder()
                .id(page.getId())
                .title(page.getTitle())
                .slug(page.getSlug())
                .content(page.getContent())
                .seoMetadata(seoDto)
                .status(page.getStatus())
                .publishedAt(page.getPublishedAt())
                .publishAt(page.getPublishAt())
                .unpublishAt(page.getUnpublishAt())
                .currentVersionNumber(page.getCurrentVersionNumber())
                .build();
    }
}
