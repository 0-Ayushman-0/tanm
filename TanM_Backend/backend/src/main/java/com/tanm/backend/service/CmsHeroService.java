package com.tanm.backend.service;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsHeroSlideDto;
import com.tanm.backend.dto.ReorderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CmsHeroService {
    List<CmsHeroSlideDto> getActiveHeroSlides();
    Page<CmsHeroSlideDto> getAllHeroSlidesAdmin(Pageable pageable);
    CmsHeroSlideDto getHeroSlideById(Long id);
    CmsHeroSlideDto createHeroSlide(CmsHeroSlideDto dto);
    CmsHeroSlideDto updateHeroSlide(Long id, CmsHeroSlideDto dto);
    void deleteHeroSlide(Long id);
    void reorderHeroSlides(ReorderRequest request);
    void handleBulkAction(BulkActionRequest request);
}
