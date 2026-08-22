package com.tanm.backend.controller;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsNavigationItemDto;
import com.tanm.backend.dto.ReorderRequest;
import com.tanm.backend.service.CmsNavigationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cms/navigation")
@RequiredArgsConstructor
public class AdminCmsNavigationController {

    private final CmsNavigationService navigationService;

    @GetMapping
    public ResponseEntity<List<CmsNavigationItemDto>> getAll() {
        return ResponseEntity.ok(navigationService.getAllNavigationItemsAdmin());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CmsNavigationItemDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(navigationService.getNavigationItemById(id));
    }

    @PostMapping
    public ResponseEntity<CmsNavigationItemDto> create(@Valid @RequestBody CmsNavigationItemDto dto) {
        return new ResponseEntity<>(navigationService.createNavigationItem(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CmsNavigationItemDto> update(@PathVariable Long id, @Valid @RequestBody CmsNavigationItemDto dto) {
        return ResponseEntity.ok(navigationService.updateNavigationItem(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        navigationService.deleteNavigationItem(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reorder")
    public ResponseEntity<Void> reorder(@Valid @RequestBody ReorderRequest request) {
        navigationService.reorderNavigationItems(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk-status")
    public ResponseEntity<Void> handleBulk(@Valid @RequestBody BulkActionRequest request) {
        navigationService.handleBulkAction(request);
        return ResponseEntity.ok().build();
    }
}
