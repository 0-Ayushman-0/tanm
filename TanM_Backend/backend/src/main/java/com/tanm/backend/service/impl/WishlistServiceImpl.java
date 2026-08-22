package com.tanm.backend.service.impl;

import com.tanm.backend.dto.WishlistDto;
import com.tanm.backend.dto.WishlistItemDto;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.entity.Product;
import com.tanm.backend.entity.Wishlist;
import com.tanm.backend.entity.WishlistItem;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.mapper.ProductMapper;
import com.tanm.backend.repository.ProductRepository;
import com.tanm.backend.repository.WishlistItemRepository;
import com.tanm.backend.repository.WishlistRepository;
import com.tanm.backend.service.CartService;
import com.tanm.backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CartService cartService;

    @Override
    @Transactional
    public WishlistDto getWishlist(AppUser user) {
        Wishlist wishlist = getOrCreateWishlist(user);
        return toDto(wishlist);
    }

    @Override
    @Transactional
    public WishlistDto addToWishlist(AppUser user, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(user);
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (!wishlistItemRepository.existsByWishlistAndProduct(wishlist, product)) {
            WishlistItem item = WishlistItem.builder()
                    .wishlist(wishlist)
                    .product(product)
                    .build();
            wishlist.getItems().add(item);
            wishlist = wishlistRepository.save(wishlist);
            log.info("User [{}] added product [{}] to wishlist", user.getEmail(), product.getSku());
        }

        return toDto(wishlist);
    }

    @Override
    @Transactional
    public WishlistDto removeFromWishlist(AppUser user, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(user);
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        wishlistItemRepository.findByWishlistAndProduct(wishlist, product).ifPresent(item -> {
            wishlist.getItems().remove(item);
            wishlistItemRepository.delete(item);
            log.info("User [{}] removed product [{}] from wishlist", user.getEmail(), product.getSku());
        });

        return toDto(wishlist);
    }

    @Override
    @Transactional
    public WishlistDto moveToCart(AppUser user, Long productId, int quantity) {
        WishlistDto updatedWishlist = removeFromWishlist(user, productId);
        cartService.addItemToUserCart(user, com.tanm.backend.dto.CartItemRequest.builder()
                .productId(productId)
                .quantity(quantity > 0 ? quantity : 1)
                .build());
        log.info("User [{}] transferred product [{}] from wishlist to shopping cart", user.getEmail(), productId);
        return updatedWishlist;
    }

    @Override
    @Transactional
    public void clearWishlist(AppUser user) {
        Wishlist wishlist = getOrCreateWishlist(user);
        wishlist.getItems().clear();
        wishlistRepository.save(wishlist);
    }

    private Wishlist getOrCreateWishlist(AppUser user) {
        return wishlistRepository.findByUser(user)
                .orElseGet(() -> wishlistRepository.save(Wishlist.builder()
                        .user(user)
                        .items(new ArrayList<>())
                        .build()));
    }

    public WishlistDto toDto(Wishlist wishlist) {
        if (wishlist == null) return null;

        List<WishlistItemDto> itemDtos = new ArrayList<>();
        if (wishlist.getItems() != null) {
            itemDtos = wishlist.getItems().stream()
                    .filter(item -> !item.isDeleted() && item.getProduct() != null && !item.getProduct().isDeleted())
                    .map(item -> WishlistItemDto.builder()
                            .id(item.getId())
                            .product(productMapper.toDto(item.getProduct()))
                            .addedAt(item.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());
        }

        return WishlistDto.builder()
                .id(wishlist.getId())
                .userId(wishlist.getUser().getId())
                .items(itemDtos)
                .totalItems(itemDtos.size())
                .build();
    }
}
