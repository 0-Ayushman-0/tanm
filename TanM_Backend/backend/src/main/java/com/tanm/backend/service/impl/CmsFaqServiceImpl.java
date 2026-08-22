package com.tanm.backend.service.impl;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsFaqDto;
import com.tanm.backend.dto.ReorderRequest;
import com.tanm.backend.entity.CmsFaq;
import com.tanm.backend.enums.CmsStatus;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.repository.CmsFaqRepository;
import com.tanm.backend.service.CmsFaqService;
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
public class CmsFaqServiceImpl implements CmsFaqService {

    private final CmsFaqRepository faqRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CmsFaqDto> getPublishedFaqs() {
        return faqRepository.findByStatusAndIsDeletedFalseOrderByDisplayOrderAsc(CmsStatus.PUBLISHED).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CmsFaqDto> getAllFaqsAdmin(Pageable pageable) {
        return faqRepository.findByIsDeletedFalse(pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CmsFaqDto getFaqById(Long id) {
        CmsFaq faq = faqRepository.findById(id)
                .filter(f -> !f.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("FAQ not found with ID: " + id));
        return toDto(faq);
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public CmsFaqDto createFaq(CmsFaqDto dto) {
        CmsFaq faq = CmsFaq.builder()
                .question(dto.getQuestion())
                .answer(dto.getAnswer())
                .category(dto.getCategory() != null ? dto.getCategory() : "General")
                .displayOrder(dto.getDisplayOrder())
                .status(dto.getStatus() != null ? dto.getStatus() : CmsStatus.PUBLISHED)
                .build();
        return toDto(faqRepository.save(faq));
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public CmsFaqDto updateFaq(Long id, CmsFaqDto dto) {
        CmsFaq faq = faqRepository.findById(id)
                .filter(f -> !f.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("FAQ not found with ID: " + id));

        faq.setQuestion(dto.getQuestion());
        faq.setAnswer(dto.getAnswer());
        if (dto.getCategory() != null) faq.setCategory(dto.getCategory());
        faq.setDisplayOrder(dto.getDisplayOrder());
        if (dto.getStatus() != null) faq.setStatus(dto.getStatus());

        return toDto(faqRepository.save(faq));
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void deleteFaq(Long id) {
        CmsFaq faq = faqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ not found with ID: " + id));
        faq.setDeleted(true);
        faqRepository.save(faq);
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void reorderFaqs(ReorderRequest request) {
        List<Long> ids = request.getOrderedIds();
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            int order = i;
            faqRepository.findById(id).ifPresent(faq -> {
                faq.setDisplayOrder(order);
                faqRepository.save(faq);
            });
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void handleBulkAction(BulkActionRequest request) {
        for (Long id : request.getIds()) {
            faqRepository.findById(id).ifPresent(faq -> {
                if ("DELETE".equalsIgnoreCase(request.getAction())) {
                    faq.setDeleted(true);
                } else if ("STATUS_CHANGE".equalsIgnoreCase(request.getAction()) && request.getStatus() != null) {
                    faq.setStatus(request.getStatus());
                }
                faqRepository.save(faq);
            });
        }
    }

    public CmsFaqDto toDto(CmsFaq faq) {
        if (faq == null) return null;
        return CmsFaqDto.builder()
                .id(faq.getId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .category(faq.getCategory())
                .displayOrder(faq.getDisplayOrder())
                .status(faq.getStatus())
                .build();
    }
}
