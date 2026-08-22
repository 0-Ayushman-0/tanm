package com.tanm.backend.controller;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsAnnouncementBarDto;
import com.tanm.backend.service.CmsAnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cms/announcements")
@RequiredArgsConstructor
public class AdminCmsAnnouncementController {

    private final CmsAnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<Page<CmsAnnouncementBarDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return ResponseEntity.ok(announcementService.getAllAnnouncementsAdmin(PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CmsAnnouncementBarDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(announcementService.getAnnouncementById(id));
    }

    @PostMapping
    public ResponseEntity<CmsAnnouncementBarDto> create(@Valid @RequestBody CmsAnnouncementBarDto dto) {
        return new ResponseEntity<>(announcementService.createAnnouncement(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CmsAnnouncementBarDto> update(@PathVariable Long id, @Valid @RequestBody CmsAnnouncementBarDto dto) {
        return ResponseEntity.ok(announcementService.updateAnnouncement(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-status")
    public ResponseEntity<Void> handleBulk(@Valid @RequestBody BulkActionRequest request) {
        announcementService.handleBulkAction(request);
        return ResponseEntity.ok().build();
    }
}
