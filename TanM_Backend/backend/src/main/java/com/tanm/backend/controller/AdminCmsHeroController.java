package com.tanm.backend.controller;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsHeroSlideDto;
import com.tanm.backend.dto.ReorderRequest;
import com.tanm.backend.service.CmsHeroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cms/hero")
@RequiredArgsConstructor
public class AdminCmsHeroController {

    private final CmsHeroService heroService;

    @GetMapping
    public ResponseEntity<Page<CmsHeroSlideDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return ResponseEntity.ok(heroService.getAllHeroSlidesAdmin(PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CmsHeroSlideDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(heroService.getHeroSlideById(id));
    }

    @PostMapping
    public ResponseEntity<CmsHeroSlideDto> create(@Valid @RequestBody CmsHeroSlideDto dto) {
        return new ResponseEntity<>(heroService.createHeroSlide(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CmsHeroSlideDto> update(@PathVariable Long id, @Valid @RequestBody CmsHeroSlideDto dto) {
        return ResponseEntity.ok(heroService.updateHeroSlide(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        heroService.deleteHeroSlide(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reorder")
    public ResponseEntity<Void> reorder(@Valid @RequestBody ReorderRequest request) {
        heroService.reorderHeroSlides(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk-status")
    public ResponseEntity<Void> handleBulk(@Valid @RequestBody BulkActionRequest request) {
        heroService.handleBulkAction(request);
        return ResponseEntity.ok().build();
    }
}
