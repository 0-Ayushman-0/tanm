package com.tanm.backend.service.impl;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsTestimonialDto;
import com.tanm.backend.dto.MediaDto;
import com.tanm.backend.dto.ReorderRequest;
import com.tanm.backend.entity.CmsTestimonial;
import com.tanm.backend.entity.Media;
import com.tanm.backend.enums.CmsStatus;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.repository.CmsTestimonialRepository;
import com.tanm.backend.repository.MediaRepository;
import com.tanm.backend.service.CmsTestimonialService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CmsTestimonialServiceImpl implements CmsTestimonialService {

    private final CmsTestimonialRepository testimonialRepository;
    private final MediaRepository mediaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CmsTestimonialDto> getPublishedTestimonials() {
        return testimonialRepository.findByStatusAndIsDeletedFalseOrderByDisplayOrderAsc(CmsStatus.PUBLISHED).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CmsTestimonialDto> getAllTestimonialsAdmin(Pageable pageable) {
        return testimonialRepository.findByIsDeletedFalse(pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CmsTestimonialDto getTestimonialById(Long id) {
        CmsTestimonial t = testimonialRepository.findById(id)
                .filter(item -> !item.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Testimonial not found with ID: " + id));
        return toDto(t);
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public CmsTestimonialDto createTestimonial(CmsTestimonialDto dto) {
        Media avatar = dto.getAvatarId() != null ? mediaRepository.findById(dto.getAvatarId()).orElse(null) : null;

        CmsTestimonial t = CmsTestimonial.builder()
                .customerName(dto.getCustomerName())
                .customerTitle(dto.getCustomerTitle())
                .rating(dto.getRating() > 0 ? dto.getRating() : 5)
                .comment(dto.getComment())
                .avatar(avatar)
                .displayOrder(dto.getDisplayOrder())
                .status(dto.getStatus() != null ? dto.getStatus() : CmsStatus.PUBLISHED)
                .build();
        return toDto(testimonialRepository.save(t));
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public CmsTestimonialDto updateTestimonial(Long id, CmsTestimonialDto dto) {
        CmsTestimonial t = testimonialRepository.findById(id)
                .filter(item -> !item.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Testimonial not found with ID: " + id));

        t.setCustomerName(dto.getCustomerName());
        t.setCustomerTitle(dto.getCustomerTitle());
        if (dto.getRating() > 0) t.setRating(dto.getRating());
        t.setComment(dto.getComment());
        t.setDisplayOrder(dto.getDisplayOrder());
        if (dto.getStatus() != null) t.setStatus(dto.getStatus());

        if (dto.getAvatarId() != null) {
            t.setAvatar(mediaRepository.findById(dto.getAvatarId()).orElse(null));
        }

        return toDto(testimonialRepository.save(t));
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void deleteTestimonial(Long id) {
        CmsTestimonial t = testimonialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonial not found with ID: " + id));
        t.setDeleted(true);
        testimonialRepository.save(t);
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void reorderTestimonials(ReorderRequest request) {
        List<Long> ids = request.getOrderedIds();
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            int order = i;
            testimonialRepository.findById(id).ifPresent(t -> {
                t.setDisplayOrder(order);
                testimonialRepository.save(t);
            });
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void handleBulkAction(BulkActionRequest request) {
        for (Long id : request.getIds()) {
            testimonialRepository.findById(id).ifPresent(t -> {
                if ("DELETE".equalsIgnoreCase(request.getAction())) {
                    t.setDeleted(true);
                } else if ("STATUS_CHANGE".equalsIgnoreCase(request.getAction()) && request.getStatus() != null) {
                    t.setStatus(request.getStatus());
                }
                testimonialRepository.save(t);
            });
        }
    }

    public CmsTestimonialDto toDto(CmsTestimonial t) {
        if (t == null) return null;
        MediaDto avatarDto = t.getAvatar() != null ? MediaDto.builder().id(t.getAvatar().getId()).url(t.getAvatar().getUrl()).build() : null;

        return CmsTestimonialDto.builder()
                .id(t.getId())
                .customerName(t.getCustomerName())
                .customerTitle(t.getCustomerTitle())
                .rating(t.getRating())
                .comment(t.getComment())
                .avatar(avatarDto)
                .avatarId(t.getAvatar() != null ? t.getAvatar().getId() : null)
                .displayOrder(t.getDisplayOrder())
                .status(t.getStatus())
                .build();
    }
}
