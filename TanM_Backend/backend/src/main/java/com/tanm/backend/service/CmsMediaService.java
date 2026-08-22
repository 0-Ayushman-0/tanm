package com.tanm.backend.service;

import com.tanm.backend.dto.MediaDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CmsMediaService {
    MediaDto saveMedia(MediaDto mediaDto);
    Page<MediaDto> getAllMedia(String folder, Pageable pageable);
    MediaDto getMediaById(Long id);
    MediaDto updateMedia(Long id, MediaDto mediaDto);
    void deleteMedia(Long id);
}
