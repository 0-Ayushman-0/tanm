package com.tanm.backend.controller;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsTestimonialDto;
import com.tanm.backend.dto.ReorderRequest;
import com.tanm.backend.service.CmsTestimonialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cms/testimonials")
@RequiredArgsConstructor
public class AdminCmsTestimonialController {

    private final CmsTestimonialService testimonialService;

    @GetMapping
    public ResponseEntity<Page<CmsTestimonialDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return ResponseEntity.ok(testimonialService.getAllTestimonialsAdmin(PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CmsTestimonialDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(testimonialService.getTestimonialById(id));
    }

    @PostMapping
    public ResponseEntity<CmsTestimonialDto> create(@Valid @RequestBody CmsTestimonialDto dto) {
        return new ResponseEntity<>(testimonialService.createTestimonial(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CmsTestimonialDto> update(@PathVariable Long id, @Valid @RequestBody CmsTestimonialDto dto) {
        return ResponseEntity.ok(testimonialService.updateTestimonial(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        testimonialService.deleteTestimonial(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reorder")
    public ResponseEntity<Void> reorder(@Valid @RequestBody ReorderRequest request) {
        testimonialService.reorderTestimonials(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk-status")
    public ResponseEntity<Void> handleBulk(@Valid @RequestBody BulkActionRequest request) {
        testimonialService.handleBulkAction(request);
        return ResponseEntity.ok().build();
    }
}
