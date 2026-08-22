package com.tanm.backend.controller;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsFooterSectionDto;
import com.tanm.backend.dto.ReorderRequest;
import com.tanm.backend.service.CmsFooterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cms/footer")
@RequiredArgsConstructor
public class AdminCmsFooterController {

    private final CmsFooterService footerService;

    @GetMapping
    public ResponseEntity<List<CmsFooterSectionDto>> getAll() {
        return ResponseEntity.ok(footerService.getAllFooterSectionsAdmin());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CmsFooterSectionDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(footerService.getFooterSectionById(id));
    }

    @PostMapping
    public ResponseEntity<CmsFooterSectionDto> create(@Valid @RequestBody CmsFooterSectionDto dto) {
        return new ResponseEntity<>(footerService.createFooterSection(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CmsFooterSectionDto> update(@PathVariable Long id, @Valid @RequestBody CmsFooterSectionDto dto) {
        return ResponseEntity.ok(footerService.updateFooterSection(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        footerService.deleteFooterSection(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reorder")
    public ResponseEntity<Void> reorder(@Valid @RequestBody ReorderRequest request) {
        footerService.reorderFooterSections(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk-status")
    public ResponseEntity<Void> handleBulk(@Valid @RequestBody BulkActionRequest request) {
        footerService.handleBulkAction(request);
        return ResponseEntity.ok().build();
    }
}
