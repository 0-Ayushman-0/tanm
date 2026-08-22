package com.tanm.backend.service.impl;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsBannerDto;
import com.tanm.backend.dto.MediaDto;
import com.tanm.backend.entity.CmsBanner;
import com.tanm.backend.entity.Media;
import com.tanm.backend.enums.BannerType;
import com.tanm.backend.enums.CmsStatus;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.repository.CmsBannerRepository;
import com.tanm.backend.repository.MediaRepository;
import com.tanm.backend.service.CmsBannerService;
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
public class CmsBannerServiceImpl implements CmsBannerService {

    private final CmsBannerRepository bannerRepository;
    private final MediaRepository mediaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CmsBannerDto> getActiveBannersByType(BannerType type) {
        return bannerRepository.findActiveBanners(type, CmsStatus.PUBLISHED, LocalDateTime.now()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CmsBannerDto> getAllBannersAdmin(Pageable pageable) {
        return bannerRepository.findByIsDeletedFalse(pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CmsBannerDto getBannerById(Long id) {
        CmsBanner banner = bannerRepository.findById(id)
                .filter(b -> !b.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found with ID: " + id));
        return toDto(banner);
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public CmsBannerDto createBanner(CmsBannerDto dto) {
        Media desktopImg = dto.getDesktopImageId() != null ? mediaRepository.findById(dto.getDesktopImageId()).orElse(null) : null;
        Media mobileImg = dto.getMobileImageId() != null ? mediaRepository.findById(dto.getMobileImageId()).orElse(null) : null;

        CmsBanner banner = CmsBanner.builder()
                .bannerType(dto.getBannerType())
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .desktopImage(desktopImg)
                .mobileImage(mobileImg)
                .buttonText(dto.getButtonText())
                .buttonUrl(dto.getButtonUrl())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .priority(dto.getPriority())
                .status(dto.getStatus() != null ? dto.getStatus() : CmsStatus.DRAFT)
                .build();

        return toDto(bannerRepository.save(banner));
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public CmsBannerDto updateBanner(Long id, CmsBannerDto dto) {
        CmsBanner banner = bannerRepository.findById(id)
                .filter(b -> !b.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found with ID: " + id));

        banner.setBannerType(dto.getBannerType());
        banner.setTitle(dto.getTitle());
        banner.setSubtitle(dto.getSubtitle());
        banner.setButtonText(dto.getButtonText());
        banner.setButtonUrl(dto.getButtonUrl());
        banner.setStartDate(dto.getStartDate());
        banner.setEndDate(dto.getEndDate());
        banner.setPriority(dto.getPriority());
        if (dto.getStatus() != null) banner.setStatus(dto.getStatus());

        if (dto.getDesktopImageId() != null) {
            banner.setDesktopImage(mediaRepository.findById(dto.getDesktopImageId()).orElse(null));
        }
        if (dto.getMobileImageId() != null) {
            banner.setMobileImage(mediaRepository.findById(dto.getMobileImageId()).orElse(null));
        }

        return toDto(bannerRepository.save(banner));
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void deleteBanner(Long id) {
        CmsBanner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found with ID: " + id));
        banner.setDeleted(true);
        bannerRepository.save(banner);
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void handleBulkAction(BulkActionRequest request) {
        for (Long id : request.getIds()) {
            bannerRepository.findById(id).ifPresent(banner -> {
                if ("DELETE".equalsIgnoreCase(request.getAction())) {
                    banner.setDeleted(true);
                } else if ("STATUS_CHANGE".equalsIgnoreCase(request.getAction()) && request.getStatus() != null) {
                    banner.setStatus(request.getStatus());
                }
                bannerRepository.save(banner);
            });
        }
    }

    public CmsBannerDto toDto(CmsBanner banner) {
        if (banner == null) return null;
        MediaDto desktopDto = banner.getDesktopImage() != null ? MediaDto.builder().id(banner.getDesktopImage().getId()).url(banner.getDesktopImage().getUrl()).build() : null;
        MediaDto mobileDto = banner.getMobileImage() != null ? MediaDto.builder().id(banner.getMobileImage().getId()).url(banner.getMobileImage().getUrl()).build() : null;

        return CmsBannerDto.builder()
                .id(banner.getId())
                .bannerType(banner.getBannerType())
                .title(banner.getTitle())
                .subtitle(banner.getSubtitle())
                .desktopImage(desktopDto)
                .desktopImageId(banner.getDesktopImage() != null ? banner.getDesktopImage().getId() : null)
                .mobileImage(mobileDto)
                .mobileImageId(banner.getMobileImage() != null ? banner.getMobileImage().getId() : null)
                .buttonText(banner.getButtonText())
                .buttonUrl(banner.getButtonUrl())
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .priority(banner.getPriority())
                .status(banner.getStatus())
                .build();
    }
}
