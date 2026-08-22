package com.tanm.backend.mapper;

import com.tanm.backend.dto.CartDto;
import com.tanm.backend.dto.CartItemDto;
import com.tanm.backend.entity.Cart;
import com.tanm.backend.entity.CartItem;
import com.tanm.backend.entity.Product;
import com.tanm.backend.enums.ProductStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartDto toDto(Cart cart) {
        if (cart == null) {
            return null;
        }

        List<CartItemDto> items = cart.getItems() != null ?
                cart.getItems().stream()
                        .map(this::toDto)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        int totalQuantity = items.stream().mapToInt(CartItemDto::getQuantity).sum();

        BigDecimal totalPrice = items.stream()
                .filter(CartItemDto::isAvailable)
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartDto.builder()
                .id(cart.getId())
                .guestToken(cart.getGuestToken())
                .couponCode(cart.getCouponCode())
                .status(cart.getStatus())
                .items(items)
                .totalQuantity(totalQuantity)
                .totalPrice(totalPrice)
                .build();
    }

    public CartItemDto toDto(CartItem item) {
        if (item == null) {
            return null;
        }

        Product product = item.getProduct();
        BigDecimal price = product.getPrice();
        int quantity = item.getQuantity();
        BigDecimal subtotal = price != null ? price.multiply(BigDecimal.valueOf(quantity)) : BigDecimal.ZERO;

        String primaryImageUrl = null;
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            primaryImageUrl = product.getImages().stream()
                    .filter(img -> !img.isDeleted() && img.isPrimary())
                    .map(img -> img.getImageUrl())
                    .findFirst()
                    .orElseGet(() -> product.getImages().stream()
                            .filter(img -> !img.isDeleted())
                            .map(img -> img.getImageUrl())
                            .findFirst()
                            .orElse(null));
        }

        boolean isAvailable = !product.isDeleted() 
                && product.isActive() 
                && product.getStatus() == ProductStatus.PUBLISHED 
                && product.getStockQuantity() > 0;

        String message = null;
        if (product.isDeleted()) {
            message = "Product discontinued";
        } else if (!product.isActive() || product.getStatus() != ProductStatus.PUBLISHED) {
            message = "Item currently unavailable";
        } else if (product.getStockQuantity() <= 0) {
            message = "Out of stock";
        } else if (product.getStockQuantity() < quantity) {
            message = "Only " + product.getStockQuantity() + " items remaining";
        }

        return CartItemDto.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .slug(product.getSlug())
                .price(price)
                .primaryImageUrl(primaryImageUrl)
                .stockRemaining(product.getStockQuantity())
                .quantity(quantity)
                .subtotal(subtotal)
                .isAvailable(isAvailable)
                .message(message)
                .build();
    }
}
