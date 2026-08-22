package com.tanm.backend.service.impl;

import com.tanm.backend.dto.ProductCreateRequest;
import com.tanm.backend.dto.ProductDto;
import com.tanm.backend.entity.Category;
import com.tanm.backend.entity.Product;
import com.tanm.backend.enums.ProductStatus;
import com.tanm.backend.exception.BadRequestException;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.mapper.ProductMapper;
import com.tanm.backend.repository.CategoryRepository;
import com.tanm.backend.repository.ProductRepository;
import com.tanm.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductDto createProduct(ProductCreateRequest request) {
        Category category = categoryRepository.findByIdAndIsDeletedFalse(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        if (productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Product with SKU '" + request.getSku() + "' already exists");
        }

        String slug = request.getName().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        if (productRepository.existsBySlug(slug)) {
            throw new BadRequestException("Product with name/slug '" + request.getName() + "' already exists");
        }

        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        } else {
            product.setStatus(ProductStatus.PUBLISHED);
        }
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductBySlug(String slug) {
        Product product = productRepository.findBySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return productMapper.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ProductDto> getAllProducts(org.springframework.data.domain.Pageable pageable) {
        return productRepository.findByIsDeletedFalse(pageable)
                .map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ProductDto> getProductsByCategory(Long categoryId, org.springframework.data.domain.Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        return productRepository.findByCategoryIdAndIsDeletedFalse(categoryId, pageable)
                .map(productMapper::toDto);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(Long id, ProductCreateRequest request) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        Category category = categoryRepository.findByIdAndIsDeletedFalse(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Optional<Product> existingBySku = productRepository.findBySkuAndIsDeletedFalse(request.getSku());
        if (existingBySku.isPresent() && !existingBySku.get().getId().equals(id)) {
            throw new BadRequestException("Product with SKU '" + request.getSku() + "' already exists");
        }

        String slug = request.getName().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        Optional<Product> existingBySlug = productRepository.findBySlugAndIsDeletedFalse(slug);
        if (existingBySlug.isPresent() && !existingBySlug.get().getId().equals(id)) {
            throw new BadRequestException("Product with name/slug '" + request.getName() + "' already exists");
        }

        productMapper.updateEntityFromRequest(request, product);
        product.setCategory(category);
        product.setSlug(slug);
        Product updatedProduct = productRepository.save(product);
        return productMapper.toDto(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        product.setDeleted(true);
        product.setStatus(ProductStatus.ARCHIVED);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ProductDto> filterProducts(com.tanm.backend.dto.ProductFilterRequest filter, org.springframework.data.domain.Pageable pageable) {
        return productRepository.findAll(com.tanm.backend.specification.ProductSpecification.filter(filter), pageable)
                .map(productMapper::toDto);
    }
}
