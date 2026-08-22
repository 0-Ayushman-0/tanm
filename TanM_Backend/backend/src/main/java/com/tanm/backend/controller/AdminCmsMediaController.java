package com.tanm.backend.controller;

import com.tanm.backend.dto.MediaDto;
import com.tanm.backend.service.CmsMediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cms/media")
@RequiredArgsConstructor
public class AdminCmsMediaController {

    private final CmsMediaService mediaService;

    @GetMapping
    public ResponseEntity<Page<MediaDto>> getAll(
            @RequestParam(required = false) String folder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return ResponseEntity.ok(mediaService.getAllMedia(folder, PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getMediaById(id));
    }

    @PostMapping
    public ResponseEntity<MediaDto> create(@Valid @RequestBody MediaDto dto) {
        return new ResponseEntity<>(mediaService.saveMedia(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MediaDto> update(@PathVariable Long id, @Valid @RequestBody MediaDto dto) {
        return ResponseEntity.ok(mediaService.updateMedia(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.noContent().build();
    }
}
