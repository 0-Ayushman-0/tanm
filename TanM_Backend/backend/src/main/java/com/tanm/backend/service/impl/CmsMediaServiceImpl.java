package com.tanm.backend.service.impl;

import com.tanm.backend.dto.MediaDto;
import com.tanm.backend.entity.Media;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.repository.MediaRepository;
import com.tanm.backend.service.CmsMediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CmsMediaServiceImpl implements CmsMediaService {

    private final MediaRepository mediaRepository;

    @Override
    @Transactional
    public MediaDto saveMedia(MediaDto dto) {
        Media media = Media.builder()
                .fileName(dto.getFileName())
                .storageKey(dto.getStorageKey())
                .url(dto.getUrl())
                .thumbnailUrl(dto.getThumbnailUrl())
                .mimeType(dto.getMimeType())
                .width(dto.getWidth())
                .height(dto.getHeight())
                .size(dto.getSize())
                .altText(dto.getAltText())
                .folder(dto.getFolder() != null ? dto.getFolder() : "general")
                .uploadedBy(dto.getUploadedBy() != null ? dto.getUploadedBy() : "SYSTEM")
                .build();
        Media saved = mediaRepository.save(media);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MediaDto> getAllMedia(String folder, Pageable pageable) {
        Page<Media> page = (folder != null && !folder.isBlank())
                ? mediaRepository.findByFolderAndIsDeletedFalse(folder, pageable)
                : mediaRepository.findByIsDeletedFalse(pageable);
        return page.map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public MediaDto getMediaById(Long id) {
        Media media = mediaRepository.findById(id)
                .filter(m -> !m.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Media asset not found with ID: " + id));
        return toDto(media);
    }

    @Override
    @Transactional
    public MediaDto updateMedia(Long id, MediaDto dto) {
        Media media = mediaRepository.findById(id)
                .filter(m -> !m.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Media asset not found with ID: " + id));

        if (dto.getAltText() != null) media.setAltText(dto.getAltText());
        if (dto.getFolder() != null) media.setFolder(dto.getFolder());

        Media updated = mediaRepository.save(media);
        return toDto(updated);
    }

    @Override
    @Transactional
    public void deleteMedia(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media asset not found with ID: " + id));
        media.setDeleted(true);
        mediaRepository.save(media);
    }

    public MediaDto toDto(Media media) {
        if (media == null) return null;
        return MediaDto.builder()
                .id(media.getId())
                .fileName(media.getFileName())
                .storageKey(media.getStorageKey())
                .url(media.getUrl())
                .thumbnailUrl(media.getThumbnailUrl())
                .mimeType(media.getMimeType())
                .width(media.getWidth())
                .height(media.getHeight())
                .size(media.getSize())
                .altText(media.getAltText())
                .folder(media.getFolder())
                .uploadedBy(media.getUploadedBy())
                .createdAt(media.getCreatedAt())
                .build();
    }
}
