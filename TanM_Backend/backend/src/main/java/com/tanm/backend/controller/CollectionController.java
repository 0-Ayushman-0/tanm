package com.tanm.backend.controller;

import com.tanm.backend.dto.CollectionCreateRequest;
import com.tanm.backend.dto.CollectionDetailDto;
import com.tanm.backend.dto.CollectionDto;
import com.tanm.backend.service.CollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @PostMapping
    public ResponseEntity<CollectionDto> createCollection(@Valid @RequestBody CollectionCreateRequest request) {
        CollectionDto created = collectionService.createCollection(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<CollectionDetailDto> getCollectionById(@PathVariable Long id) {
        CollectionDetailDto collection = collectionService.getCollectionById(id);
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/{slug:[a-zA-Z0-9-]*[a-zA-Z-][a-zA-Z0-9-]*}")
    public ResponseEntity<CollectionDetailDto> getCollectionBySlug(@PathVariable String slug) {
        CollectionDetailDto collection = collectionService.getCollectionBySlug(slug);
        return ResponseEntity.ok(collection);
    }

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<CollectionDto>> getAllCollections(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
                ? org.springframework.data.domain.Sort.by(sortBy).descending()
                : org.springframework.data.domain.Sort.by(sortBy).ascending();
        return ResponseEntity.ok(collectionService.getAllCollections(org.springframework.data.domain.PageRequest.of(page, size, sort)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollectionDto> updateCollection(
            @PathVariable Long id,
            @Valid @RequestBody CollectionCreateRequest request) {
        CollectionDto updated = collectionService.updateCollection(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {
        collectionService.deleteCollection(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/products/{productId}")
    public ResponseEntity<Void> addProductToCollection(
            @PathVariable Long id,
            @PathVariable Long productId) {
        collectionService.addProductToCollection(id, productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/products/{productId}")
    public ResponseEntity<Void> removeProductFromCollection(
            @PathVariable Long id,
            @PathVariable Long productId) {
        collectionService.removeProductFromCollection(id, productId);
        return ResponseEntity.noContent().build();
    }
}
