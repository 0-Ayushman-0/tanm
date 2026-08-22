package com.tanm.backend.mapper;

import com.tanm.backend.dto.CollectionCreateRequest;
import com.tanm.backend.dto.CollectionDetailDto;
import com.tanm.backend.dto.CollectionDto;
import com.tanm.backend.entity.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CollectionMapper {

    private final ProductMapper productMapper;

    public CollectionDto toDto(Collection collection) {
        if (collection == null) {
            return null;
        }
        return CollectionDto.builder()
                .id(collection.getId())
                .name(collection.getName())
                .description(collection.getDescription())
                .slug(collection.getSlug())
                .imageUrl(collection.getImageUrl())
                .displayOrder(collection.getDisplayOrder())
                .isFeatured(collection.isFeatured())
                .isActive(collection.isActive())
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .build();
    }

    public CollectionDetailDto toDetailDto(Collection collection) {
        if (collection == null) {
            return null;
        }
        return CollectionDetailDto.builder()
                .id(collection.getId())
                .name(collection.getName())
                .description(collection.getDescription())
                .slug(collection.getSlug())
                .imageUrl(collection.getImageUrl())
                .displayOrder(collection.getDisplayOrder())
                .isFeatured(collection.isFeatured())
                .isActive(collection.isActive())
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .products(collection.getProducts() != null ? collection.getProducts().stream()
                        .filter(product -> !product.isDeleted())
                        .map(productMapper::toDto)
                        .collect(Collectors.toList()) : null)
                .build();
    }

    public Collection toEntity(CollectionCreateRequest request) {
        if (request == null) {
            return null;
        }
        return Collection.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .displayOrder(request.getDisplayOrder())
                .isFeatured(request.isFeatured())
                .build();
    }

    public void updateEntityFromRequest(CollectionCreateRequest request, Collection collection) {
        if (request == null || collection == null) {
            return;
        }
        collection.setName(request.getName());
        collection.setDescription(request.getDescription());
        collection.setImageUrl(request.getImageUrl());
        collection.setDisplayOrder(request.getDisplayOrder());
        collection.setFeatured(request.isFeatured());
    }
}
