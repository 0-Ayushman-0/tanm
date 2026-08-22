package com.tanm.backend.controller;

import com.tanm.backend.dto.CategoryDto;
import com.tanm.backend.dto.CollectionDto;
import com.tanm.backend.dto.GlobalSearchResponseDto;
import com.tanm.backend.dto.ProductDto;
import com.tanm.backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<GlobalSearchResponseDto> globalSearch(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(searchService.globalSearch(q, limit));
    }

    @GetMapping("/products")
    public ResponseEntity<Page<ProductDto>> searchProducts(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return ResponseEntity.ok(searchService.searchProducts(q, PageRequest.of(page, size, sort)));
    }

    @GetMapping("/collections")
    public ResponseEntity<Page<CollectionDto>> searchCollections(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return ResponseEntity.ok(searchService.searchCollections(q, PageRequest.of(page, size, sort)));
    }

    @GetMapping("/categories")
    public ResponseEntity<Page<CategoryDto>> searchCategories(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return ResponseEntity.ok(searchService.searchCategories(q, PageRequest.of(page, size, sort)));
    }
}
