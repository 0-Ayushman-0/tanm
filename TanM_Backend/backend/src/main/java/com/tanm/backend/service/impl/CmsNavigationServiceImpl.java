package com.tanm.backend.service.impl;

import com.tanm.backend.dto.BulkActionRequest;
import com.tanm.backend.dto.CmsNavigationItemDto;
import com.tanm.backend.dto.ReorderRequest;
import com.tanm.backend.entity.CmsNavigationItem;
import com.tanm.backend.enums.CmsStatus;
import com.tanm.backend.enums.MenuType;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.repository.CmsNavigationItemRepository;
import com.tanm.backend.service.CmsNavigationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CmsNavigationServiceImpl implements CmsNavigationService {

    private final CmsNavigationItemRepository navigationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CmsNavigationItemDto> getActiveNavigationTree() {
        return navigationRepository.findByParentIsNullAndStatusAndIsDeletedFalseOrderByDisplayOrderAsc(CmsStatus.PUBLISHED).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CmsNavigationItemDto> getAllNavigationItemsAdmin() {
        return navigationRepository.findByParentIsNullAndIsDeletedFalseOrderByDisplayOrderAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CmsNavigationItemDto getNavigationItemById(Long id) {
        CmsNavigationItem item = navigationRepository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Navigation item not found with ID: " + id));
        return toDto(item);
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public CmsNavigationItemDto createNavigationItem(CmsNavigationItemDto dto) {
        CmsNavigationItem parent = dto.getParentId() != null ? navigationRepository.findById(dto.getParentId()).orElse(null) : null;

        CmsNavigationItem item = CmsNavigationItem.builder()
                .label(dto.getLabel())
                .url(dto.getUrl())
                .parent(parent)
                .displayOrder(dto.getDisplayOrder())
                .menuType(dto.getMenuType() != null ? dto.getMenuType() : MenuType.CUSTOM)
                .targetId(dto.getTargetId())
                .isExternal(dto.isExternal())
                .icon(dto.getIcon())
                .status(dto.getStatus() != null ? dto.getStatus() : CmsStatus.PUBLISHED)
                .build();

        return toDto(navigationRepository.save(item));
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public CmsNavigationItemDto updateNavigationItem(Long id, CmsNavigationItemDto dto) {
        CmsNavigationItem item = navigationRepository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Navigation item not found with ID: " + id));

        item.setLabel(dto.getLabel());
        item.setUrl(dto.getUrl());
        item.setDisplayOrder(dto.getDisplayOrder());
        if (dto.getMenuType() != null) item.setMenuType(dto.getMenuType());
        item.setTargetId(dto.getTargetId());
        item.setExternal(dto.isExternal());
        item.setIcon(dto.getIcon());
        if (dto.getStatus() != null) item.setStatus(dto.getStatus());

        if (dto.getParentId() != null) {
            item.setParent(navigationRepository.findById(dto.getParentId()).orElse(null));
        } else {
            item.setParent(null);
        }

        return toDto(navigationRepository.save(item));
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void deleteNavigationItem(Long id) {
        CmsNavigationItem item = navigationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Navigation item not found with ID: " + id));
        item.setDeleted(true);
        navigationRepository.save(item);
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void reorderNavigationItems(ReorderRequest request) {
        List<Long> ids = request.getOrderedIds();
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            int order = i;
            navigationRepository.findById(id).ifPresent(item -> {
                item.setDisplayOrder(order);
                navigationRepository.save(item);
            });
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void handleBulkAction(BulkActionRequest request) {
        for (Long id : request.getIds()) {
            navigationRepository.findById(id).ifPresent(item -> {
                if ("DELETE".equalsIgnoreCase(request.getAction())) {
                    item.setDeleted(true);
                } else if ("STATUS_CHANGE".equalsIgnoreCase(request.getAction()) && request.getStatus() != null) {
                    item.setStatus(request.getStatus());
                }
                navigationRepository.save(item);
            });
        }
    }

    public CmsNavigationItemDto toDto(CmsNavigationItem item) {
        if (item == null) return null;

        List<CmsNavigationItemDto> childrenDto = new ArrayList<>();
        if (item.getChildren() != null) {
            childrenDto = item.getChildren().stream()
                    .filter(c -> !c.isDeleted() && c.getStatus() == CmsStatus.PUBLISHED)
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }

        return CmsNavigationItemDto.builder()
                .id(item.getId())
                .label(item.getLabel())
                .url(item.getUrl())
                .parentId(item.getParent() != null ? item.getParent().getId() : null)
                .children(childrenDto)
                .displayOrder(item.getDisplayOrder())
                .menuType(item.getMenuType())
                .targetId(item.getTargetId())
                .isExternal(item.isExternal())
                .icon(item.getIcon())
                .status(item.getStatus())
                .build();
    }
}
