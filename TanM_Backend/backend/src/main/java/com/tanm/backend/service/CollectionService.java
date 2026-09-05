package com.tanm.backend.service;

import com.tanm.backend.dto.CollectionCreateRequest;
import com.tanm.backend.dto.CollectionDetailDto;
import com.tanm.backend.dto.CollectionDto;

public interface CollectionService {
    CollectionDto createCollection(CollectionCreateRequest request);
    CollectionDetailDto getCollectionById(Long id);
    CollectionDetailDto getCollectionBySlug(String slug);
    org.springframework.data.domain.Page<CollectionDto> getAllCollections(org.springframework.data.domain.Pageable pageable);
    CollectionDto updateCollection(Long id, CollectionCreateRequest request);
    void deleteCollection(Long id);
    void addProductToCollection(Long collectionId, Long productId);
    void removeProductFromCollection(Long collectionId, Long productId);
}
