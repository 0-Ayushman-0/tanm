package com.tanm.backend.controller;

import com.tanm.backend.dto.CartDto;
import com.tanm.backend.dto.CartItemRequest;
import com.tanm.backend.dto.CartMergeRequest;
import com.tanm.backend.dto.CartQuantityPatchRequest;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.exception.BadRequestException;
import com.tanm.backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto> getCart(
            @RequestHeader(value = "Guest-Token", required = false) String guestToken) {
        AppUser user = getAuthenticatedUser();
        validateIdentifiers(user, guestToken);

        if (user != null) {
            return ResponseEntity.ok(cartService.getOrCreateUserCart(user));
        } else {
            return ResponseEntity.ok(cartService.getOrCreateGuestCart(guestToken));
        }
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItemToCart(
            @Valid @RequestBody CartItemRequest request,
            @RequestHeader(value = "Guest-Token", required = false) String guestToken) {
        AppUser user = getAuthenticatedUser();
        validateIdentifiers(user, guestToken);

        if (user != null) {
            return ResponseEntity.ok(cartService.addItemToUserCart(user, request));
        } else {
            return ResponseEntity.ok(cartService.addItemToGuestCart(guestToken, request));
        }
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartDto> patchCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody CartQuantityPatchRequest request,
            @RequestHeader(value = "Guest-Token", required = false) String guestToken) {
        AppUser user = getAuthenticatedUser();
        validateIdentifiers(user, guestToken);

        if (user != null) {
            return ResponseEntity.ok(cartService.patchUserCartItem(user, itemId, request));
        } else {
            return ResponseEntity.ok(cartService.patchGuestCartItem(guestToken, itemId, request));
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDto> removeCartItem(
            @PathVariable Long itemId,
            @RequestHeader(value = "Guest-Token", required = false) String guestToken) {
        AppUser user = getAuthenticatedUser();
        validateIdentifiers(user, guestToken);

        if (user != null) {
            return ResponseEntity.ok(cartService.removeUserCartItem(user, itemId));
        } else {
            return ResponseEntity.ok(cartService.removeGuestCartItem(guestToken, itemId));
        }
    }

    @DeleteMapping
    public ResponseEntity<CartDto> clearCart(
            @RequestHeader(value = "Guest-Token", required = false) String guestToken) {
        AppUser user = getAuthenticatedUser();
        validateIdentifiers(user, guestToken);

        if (user != null) {
            return ResponseEntity.ok(cartService.clearUserCart(user));
        } else {
            return ResponseEntity.ok(cartService.clearGuestCart(guestToken));
        }
    }

    @PostMapping("/merge")
    public ResponseEntity<CartDto> mergeCart(@Valid @RequestBody CartMergeRequest request) {
        AppUser user = getAuthenticatedUser();
        if (user == null) {
            throw new BadRequestException("Must be authenticated to merge carts");
        }
        return ResponseEntity.ok(cartService.mergeCarts(user, request.getGuestToken()));
    }

    private void validateIdentifiers(AppUser user, String guestToken) {
        if (user == null && (guestToken == null || guestToken.trim().isEmpty())) {
            throw new BadRequestException("No active session identification provided (Bearer Token or Guest-Token required)");
        }
    }

    private AppUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AppUser) {
            return (AppUser) authentication.getPrincipal();
        }
        return null;
    }
}
