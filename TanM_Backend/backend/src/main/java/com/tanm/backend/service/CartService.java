package com.tanm.backend.service;

import com.tanm.backend.dto.CartDto;
import com.tanm.backend.dto.CartItemRequest;
import com.tanm.backend.dto.CartQuantityPatchRequest;
import com.tanm.backend.entity.AppUser;

public interface CartService {
    CartDto getOrCreateUserCart(AppUser user);
    CartDto getOrCreateGuestCart(String guestToken);
    CartDto addItemToUserCart(AppUser user, CartItemRequest request);
    CartDto addItemToGuestCart(String guestToken, CartItemRequest request);
    CartDto patchUserCartItem(AppUser user, Long itemId, CartQuantityPatchRequest request);
    CartDto patchGuestCartItem(String guestToken, Long itemId, CartQuantityPatchRequest request);
    CartDto removeUserCartItem(AppUser user, Long itemId);
    CartDto removeGuestCartItem(String guestToken, Long itemId);
    CartDto clearUserCart(AppUser user);
    CartDto clearGuestCart(String guestToken);
    CartDto mergeCarts(AppUser user, String guestToken);
}
