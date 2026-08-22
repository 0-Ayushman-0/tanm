package com.tanm.backend.controller;

import com.tanm.backend.dto.WishlistDto;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<WishlistDto> getWishlist(@AuthenticationPrincipal AppUser user) {
        return ResponseEntity.ok(wishlistService.getWishlist(user));
    }

    @PostMapping("/items/{productId}")
    public ResponseEntity<WishlistDto> addToWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal AppUser user
    ) {
        return ResponseEntity.ok(wishlistService.addToWishlist(user, productId));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<WishlistDto> removeFromWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal AppUser user
    ) {
        return ResponseEntity.ok(wishlistService.removeFromWishlist(user, productId));
    }

    @PostMapping("/items/{productId}/move-to-cart")
    public ResponseEntity<WishlistDto> moveToCart(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            @AuthenticationPrincipal AppUser user
    ) {
        return ResponseEntity.ok(wishlistService.moveToCart(user, productId, quantity));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearWishlist(@AuthenticationPrincipal AppUser user) {
        wishlistService.clearWishlist(user);
        return ResponseEntity.noContent().build();
    }
}
