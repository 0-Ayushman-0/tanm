package com.tanm.backend.controller;

import com.tanm.backend.dto.ProductCreateRequest;
import com.tanm.backend.dto.ProductDto;
import com.tanm.backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final com.tanm.backend.service.ProductImageService productImageService;

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        ProductDto created = productService.createProduct(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<ProductDto>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
                ? org.springframework.data.domain.Sort.by(sortBy).descending()
                : org.springframework.data.domain.Sort.by(sortBy).ascending();
        return ResponseEntity.ok(productService.getAllProducts(org.springframework.data.domain.PageRequest.of(page, size, sort)));
    }

    @GetMapping("/filter")
    public ResponseEntity<org.springframework.data.domain.Page<ProductDto>> filterProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long collectionId,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) String leatherType,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Boolean inStockOnly,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        com.tanm.backend.dto.ProductFilterRequest filter = com.tanm.backend.dto.ProductFilterRequest.builder()
                .categoryId(categoryId)
                .collectionId(collectionId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .leatherType(leatherType)
                .color(color)
                .inStockOnly(inStockOnly)
                .isFeatured(isFeatured)
                .sortBy(sortBy)
                .build();

        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
                ? org.springframework.data.domain.Sort.by(sortBy).descending()
                : org.springframework.data.domain.Sort.by(sortBy).ascending();

        return ResponseEntity.ok(productService.filterProducts(filter, org.springframework.data.domain.PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        ProductDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/{slug:[a-zA-Z0-9-]*[a-zA-Z-][a-zA-Z0-9-]*}")
    public ResponseEntity<ProductDto> getProductBySlug(@PathVariable String slug) {
        ProductDto product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<org.springframework.data.domain.Page<ProductDto>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
                ? org.springframework.data.domain.Sort.by(sortBy).descending()
                : org.springframework.data.domain.Sort.by(sortBy).ascending();
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, org.springframework.data.domain.PageRequest.of(page, size, sort)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductCreateRequest request) {
        ProductDto updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<com.tanm.backend.dto.ProductImageDto> addProductImage(
            @PathVariable Long id,
            @Valid @RequestBody com.tanm.backend.dto.ProductImageAddRequest request) {
        com.tanm.backend.dto.ProductImageDto created = productImageService.addProductImage(id, request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/images/bulk")
    public ResponseEntity<java.util.List<com.tanm.backend.dto.ProductImageDto>> addProductImagesBulk(
            @PathVariable Long id,
            @Valid @RequestBody java.util.List<com.tanm.backend.dto.ProductImageAddRequest> requests) {
        java.util.List<com.tanm.backend.dto.ProductImageDto> createdList = productImageService.addProductImagesBulk(id, requests);
        return new ResponseEntity<>(createdList, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> removeProductImage(
            @PathVariable Long id,
            @PathVariable Long imageId) {
        productImageService.removeProductImage(id, imageId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/images/order")
    public ResponseEntity<Void> reorderProductImages(
            @PathVariable Long id,
            @Valid @RequestBody com.tanm.backend.dto.ProductImageReorderRequest request) {
        productImageService.reorderProductImages(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/images/{imageId}/primary")
    public ResponseEntity<com.tanm.backend.dto.ProductImageDto> setPrimaryImage(
            @PathVariable Long id,
            @PathVariable Long imageId) {
        com.tanm.backend.dto.ProductImageDto updated = productImageService.setPrimaryImage(id, imageId);
        return ResponseEntity.ok(updated);
    }
}
