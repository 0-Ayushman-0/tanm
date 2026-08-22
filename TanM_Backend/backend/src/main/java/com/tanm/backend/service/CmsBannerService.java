package com.tanm.backend.service;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsBannerDto;
import com.tanm.backend.enums.BannerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CmsBannerService {
    List<CmsBannerDto> getActiveBannersByType(BannerType type);
    Page<CmsBannerDto> getAllBannersAdmin(Pageable pageable);
    CmsBannerDto getBannerById(Long id);
    CmsBannerDto createBanner(CmsBannerDto dto);
    CmsBannerDto updateBanner(Long id, CmsBannerDto dto);
    void deleteBanner(Long id);
    void handleBulkAction(BulkActionRequest request);
}
