package com.tanm.backend.service;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsFaqDto;
import com.tanm.backend.dto.ReorderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CmsFaqService {
    List<CmsFaqDto> getPublishedFaqs();
    Page<CmsFaqDto> getAllFaqsAdmin(Pageable pageable);
    CmsFaqDto getFaqById(Long id);
    CmsFaqDto createFaq(CmsFaqDto dto);
    CmsFaqDto updateFaq(Long id, CmsFaqDto dto);
    void deleteFaq(Long id);
    void reorderFaqs(ReorderRequest request);
    void handleBulkAction(BulkActionRequest request);
}
