package com.tanm.backend.controller;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsPageVersionDto;
import com.tanm.backend.dto.CmsStaticPageDto;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.service.CmsPageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cms/pages")
@RequiredArgsConstructor
public class AdminCmsPageController {

    private final CmsPageService pageService;

    @GetMapping
    public ResponseEntity<Page<CmsStaticPageDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return ResponseEntity.ok(pageService.getAllPagesAdmin(PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CmsStaticPageDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(pageService.getPageById(id));
    }

    @PostMapping
    public ResponseEntity<CmsStaticPageDto> create(@Valid @RequestBody CmsStaticPageDto dto) {
        return new ResponseEntity<>(pageService.createPage(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CmsStaticPageDto> update(@PathVariable Long id, @Valid @RequestBody CmsStaticPageDto dto) {
        return ResponseEntity.ok(pageService.updatePage(id, dto));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<CmsStaticPageDto> publish(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser user
    ) {
        String adminEmail = user != null ? user.getEmail() : "ADMIN";
        return ResponseEntity.ok(pageService.publishPage(id, adminEmail));
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<Page<CmsPageVersionDto>> getVersions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(pageService.getPageVersions(id, PageRequest.of(page, size)));
    }

    @PostMapping("/{id}/rollback/{versionNumber}")
    public ResponseEntity<CmsStaticPageDto> rollback(
            @PathVariable Long id,
            @PathVariable int versionNumber,
            @AuthenticationPrincipal AppUser user
    ) {
        String adminEmail = user != null ? user.getEmail() : "ADMIN";
        return ResponseEntity.ok(pageService.rollbackToVersion(id, versionNumber, adminEmail));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pageService.deletePage(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-status")
    public ResponseEntity<Void> handleBulk(@Valid @RequestBody BulkActionRequest request) {
        pageService.handleBulkAction(request);
        return ResponseEntity.ok().build();
    }
}
