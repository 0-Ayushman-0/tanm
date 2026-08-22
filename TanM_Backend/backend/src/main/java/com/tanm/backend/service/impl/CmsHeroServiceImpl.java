package com.tanm.backend.service.impl;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsHeroSlideDto;
import com.tanm.backend.dto.MediaDto;
import com.tanm.backend.dto.ReorderRequest;
import com.tanm.backend.entity.CmsHeroSlide;
import com.tanm.backend.entity.Media;
import com.tanm.backend.enums.CmsStatus;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.repository.CmsHeroSlideRepository;
import com.tanm.backend.repository.MediaRepository;
import com.tanm.backend.service.CmsHeroService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CmsHeroServiceImpl implements CmsHeroService {

    private final CmsHeroSlideRepository heroSlideRepository;
    private final MediaRepository mediaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CmsHeroSlideDto> getActiveHeroSlides() {
        return heroSlideRepository.findActiveHeroSlides(CmsStatus.PUBLISHED, LocalDateTime.now()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CmsHeroSlideDto> getAllHeroSlidesAdmin(Pageable pageable) {
        return heroSlideRepository.findByIsDeletedFalse(pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CmsHeroSlideDto getHeroSlideById(Long id) {
        CmsHeroSlide slide = heroSlideRepository.findById(id)
                .filter(s -> !s.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Hero slide not found with ID: " + id));
        return toDto(slide);
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public CmsHeroSlideDto createHeroSlide(CmsHeroSlideDto dto) {
        Media bgImage = null;
        if (dto.getBackgroundImageId() != null) {
            bgImage = mediaRepository.findById(dto.getBackgroundImageId()).orElse(null);
        } else if (dto.getBackgroundImage() != null && dto.getBackgroundImage().getUrl() != null) {
            bgImage = Media.builder()
                    .fileName("hero_image.jpg")
                    .storageKey("key_" + java.util.UUID.randomUUID().toString())
                    .url(dto.getBackgroundImage().getUrl())
                    .mimeType("image/jpeg")
                    .size(0L)
                    .build();
            bgImage = mediaRepository.save(bgImage);
        }

        Media mobImage = dto.getMobileImageId() != null ? mediaRepository.findById(dto.getMobileImageId()).orElse(null) : null;

        CmsHeroSlide slide = CmsHeroSlide.builder()
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .description(dto.getDescription())
                .primaryCtaText(dto.getPrimaryCtaText())
                .primaryCtaUrl(dto.getPrimaryCtaUrl())
                .secondaryCtaText(dto.getSecondaryCtaText())
                .secondaryCtaUrl(dto.getSecondaryCtaUrl())
                .backgroundImage(bgImage)
                .mobileImage(mobImage)
                .videoUrl(dto.getVideoUrl())
                .overlayOpacity(dto.getOverlayOpacity() != null ? dto.getOverlayOpacity() : 0.4)
                .sortOrder(dto.getSortOrder())
                .status(dto.getStatus() != null ? dto.getStatus() : CmsStatus.DRAFT)
                .publishAt(dto.getPublishAt())
                .unpublishAt(dto.getUnpublishAt())
                .build();

        return toDto(heroSlideRepository.save(slide));
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public CmsHeroSlideDto updateHeroSlide(Long id, CmsHeroSlideDto dto) {
        CmsHeroSlide slide = heroSlideRepository.findById(id)
                .filter(s -> !s.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Hero slide not found with ID: " + id));

        slide.setTitle(dto.getTitle());
        slide.setSubtitle(dto.getSubtitle());
        slide.setDescription(dto.getDescription());
        slide.setPrimaryCtaText(dto.getPrimaryCtaText());
        slide.setPrimaryCtaUrl(dto.getPrimaryCtaUrl());
        slide.setSecondaryCtaText(dto.getSecondaryCtaText());
        slide.setSecondaryCtaUrl(dto.getSecondaryCtaUrl());
        slide.setVideoUrl(dto.getVideoUrl());
        if (dto.getOverlayOpacity() != null) slide.setOverlayOpacity(dto.getOverlayOpacity());
        slide.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) slide.setStatus(dto.getStatus());
        slide.setPublishAt(dto.getPublishAt());
        slide.setUnpublishAt(dto.getUnpublishAt());

        if (dto.getBackgroundImageId() != null) {
            slide.setBackgroundImage(mediaRepository.findById(dto.getBackgroundImageId()).orElse(null));
        } else if (dto.getBackgroundImage() != null && dto.getBackgroundImage().getUrl() != null) {
            Media bgImage = Media.builder()
                    .fileName("hero_image.jpg")
                    .storageKey("key_" + java.util.UUID.randomUUID().toString())
                    .url(dto.getBackgroundImage().getUrl())
                    .mimeType("image/jpeg")
                    .size(0L)
                    .build();
            bgImage = mediaRepository.save(bgImage);
            slide.setBackgroundImage(bgImage);
        }

        if (dto.getMobileImageId() != null) {
            slide.setMobileImage(mediaRepository.findById(dto.getMobileImageId()).orElse(null));
        }

        return toDto(heroSlideRepository.save(slide));
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void deleteHeroSlide(Long id) {
        CmsHeroSlide slide = heroSlideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hero slide not found with ID: " + id));
        slide.setDeleted(true);
        heroSlideRepository.save(slide);
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void reorderHeroSlides(ReorderRequest request) {
        List<Long> ids = request.getOrderedIds();
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            int order = i;
            heroSlideRepository.findById(id).ifPresent(slide -> {
                slide.setSortOrder(order);
                heroSlideRepository.save(slide);
            });
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void handleBulkAction(BulkActionRequest request) {
        for (Long id : request.getIds()) {
            heroSlideRepository.findById(id).ifPresent(slide -> {
                if ("DELETE".equalsIgnoreCase(request.getAction())) {
                    slide.setDeleted(true);
                } else if ("STATUS_CHANGE".equalsIgnoreCase(request.getAction()) && request.getStatus() != null) {
                    slide.setStatus(request.getStatus());
                }
                heroSlideRepository.save(slide);
            });
        }
    }

    public CmsHeroSlideDto toDto(CmsHeroSlide slide) {
        if (slide == null) return null;
        MediaDto bgDto = slide.getBackgroundImage() != null ? MediaDto.builder().id(slide.getBackgroundImage().getId()).url(slide.getBackgroundImage().getUrl()).build() : null;
        MediaDto mobDto = slide.getMobileImage() != null ? MediaDto.builder().id(slide.getMobileImage().getId()).url(slide.getMobileImage().getUrl()).build() : null;

        return CmsHeroSlideDto.builder()
                .id(slide.getId())
                .title(slide.getTitle())
                .subtitle(slide.getSubtitle())
                .description(slide.getDescription())
                .primaryCtaText(slide.getPrimaryCtaText())
                .primaryCtaUrl(slide.getPrimaryCtaUrl())
                .secondaryCtaText(slide.getSecondaryCtaText())
                .secondaryCtaUrl(slide.getSecondaryCtaUrl())
                .backgroundImage(bgDto)
                .backgroundImageId(slide.getBackgroundImage() != null ? slide.getBackgroundImage().getId() : null)
                .mobileImage(mobDto)
                .mobileImageId(slide.getMobileImage() != null ? slide.getMobileImage().getId() : null)
                .videoUrl(slide.getVideoUrl())
                .overlayOpacity(slide.getOverlayOpacity())
                .sortOrder(slide.getSortOrder())
                .status(slide.getStatus())
                .publishAt(slide.getPublishAt())
                .unpublishAt(slide.getUnpublishAt())
                .build();
    }
}
