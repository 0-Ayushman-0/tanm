package com.tanm.backend.controller;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsFaqDto;
import com.tanm.backend.dto.ReorderRequest;
import com.tanm.backend.service.CmsFaqService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cms/faqs")
@RequiredArgsConstructor
public class AdminCmsFaqController {

    private final CmsFaqService faqService;

    @GetMapping
    public ResponseEntity<Page<CmsFaqDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return ResponseEntity.ok(faqService.getAllFaqsAdmin(PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CmsFaqDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(faqService.getFaqById(id));
    }

    @PostMapping
    public ResponseEntity<CmsFaqDto> create(@Valid @RequestBody CmsFaqDto dto) {
        return new ResponseEntity<>(faqService.createFaq(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CmsFaqDto> update(@PathVariable Long id, @Valid @RequestBody CmsFaqDto dto) {
        return ResponseEntity.ok(faqService.updateFaq(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        faqService.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reorder")
    public ResponseEntity<Void> reorder(@Valid @RequestBody ReorderRequest request) {
        faqService.reorderFaqs(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk-status")
    public ResponseEntity<Void> handleBulk(@Valid @RequestBody BulkActionRequest request) {
        faqService.handleBulkAction(request);
        return ResponseEntity.ok().build();
    }
}
