package com.tanm.backend.service;

import com.tanm.backend.dto.WishlistDto;
import com.tanm.backend.entity.AppUser;

public interface WishlistService {
    WishlistDto getWishlist(AppUser user);
    WishlistDto addToWishlist(AppUser user, Long productId);
    WishlistDto removeFromWishlist(AppUser user, Long productId);
    WishlistDto moveToCart(AppUser user, Long productId, int quantity);
    void clearWishlist(AppUser user);
}
