package com.tanm.backend.service.impl;

import com.tanm.backend.dto.CollectionCreateRequest;
import com.tanm.backend.dto.CollectionDetailDto;
import com.tanm.backend.dto.CollectionDto;
import com.tanm.backend.entity.Collection;
import com.tanm.backend.entity.Product;
import com.tanm.backend.exception.BadRequestException;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.mapper.CollectionMapper;
import com.tanm.backend.repository.CollectionRepository;
import com.tanm.backend.repository.ProductRepository;
import com.tanm.backend.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepository collectionRepository;
    private final ProductRepository productRepository;
    private final CollectionMapper collectionMapper;

    @Override
    @Transactional
    public CollectionDto createCollection(CollectionCreateRequest request) {
        if (collectionRepository.existsByName(request.getName())) {
            throw new BadRequestException("Collection with name '" + request.getName() + "' already exists");
        }

        String slug = request.getName().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        if (collectionRepository.existsBySlug(slug)) {
            throw new BadRequestException("Collection with name/slug '" + request.getName() + "' already exists");
        }

        Collection collection = collectionMapper.toEntity(request);

        // Notion-style display order logic
        if (collection.getDisplayOrder() == null || collection.getDisplayOrder() == 0) {
            long activeCount = collectionRepository.findAllByIsDeletedFalseOrderByDisplayOrderAscIdAsc().size();
            collection.setDisplayOrder((int) (activeCount + 1) * 10);
        }

        Collection saved = collectionRepository.save(collection);
        return collectionMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionDetailDto getCollectionById(Long id) {
        Collection collection = collectionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found with id: " + id));
        return collectionMapper.toDetailDto(collection);
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionDetailDto getCollectionBySlug(String slug) {
        Collection collection = collectionRepository.findBySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found with slug: " + slug));
        return collectionMapper.toDetailDto(collection);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<CollectionDto> getAllCollections(org.springframework.data.domain.Pageable pageable) {
        return collectionRepository.findByIsDeletedFalse(pageable)
                .map(collectionMapper::toDto);
    }

    @Override
    @Transactional
    public CollectionDto updateCollection(Long id, CollectionCreateRequest request) {
        Collection collection = collectionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found with id: " + id));

        Optional<Collection> existingByName = collectionRepository.findByName(request.getName());
        if (existingByName.isPresent() && !existingByName.get().getId().equals(id)) {
            throw new BadRequestException("Collection with name '" + request.getName() + "' already exists");
        }

        String slug = request.getName().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        Optional<Collection> existingBySlug = collectionRepository.findBySlugAndIsDeletedFalse(slug);
        if (existingBySlug.isPresent() && !existingBySlug.get().getId().equals(id)) {
            throw new BadRequestException("Collection with name/slug '" + request.getName() + "' already exists");
        }

        collectionMapper.updateEntityFromRequest(request, collection);
        Collection updated = collectionRepository.save(collection);
        return collectionMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteCollection(Long id) {
        Collection collection = collectionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found with id: " + id));

        collection.setDeleted(true);
        collection.getProducts().clear(); // Disconnect associated products
        collectionRepository.save(collection);
    }

    @Override
    @Transactional
    public void addProductToCollection(Long collectionId, Long productId) {
        Collection collection = collectionRepository.findByIdAndIsDeletedFalse(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found with id: " + collectionId));

        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (!collection.getProducts().contains(product)) {
            collection.getProducts().add(product);
            collectionRepository.save(collection);
        }
    }

    @Override
    @Transactional
    public void removeProductFromCollection(Long collectionId, Long productId) {
        Collection collection = collectionRepository.findByIdAndIsDeletedFalse(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found with id: " + collectionId));

        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (collection.getProducts().contains(product)) {
            collection.getProducts().remove(product);
            collectionRepository.save(collection);
        }
    }
}
