package com.tanm.backend.service;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsTestimonialDto;
import com.tanm.backend.dto.ReorderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CmsTestimonialService {
    List<CmsTestimonialDto> getPublishedTestimonials();
    Page<CmsTestimonialDto> getAllTestimonialsAdmin(Pageable pageable);
    CmsTestimonialDto getTestimonialById(Long id);
    CmsTestimonialDto createTestimonial(CmsTestimonialDto dto);
    CmsTestimonialDto updateTestimonial(Long id, CmsTestimonialDto dto);
    void deleteTestimonial(Long id);
    void reorderTestimonials(ReorderRequest request);
    void handleBulkAction(BulkActionRequest request);
}
