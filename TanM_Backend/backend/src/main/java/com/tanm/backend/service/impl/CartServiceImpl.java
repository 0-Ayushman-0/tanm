package com.tanm.backend.service.impl;

import com.tanm.backend.dto.CartDto;
import com.tanm.backend.dto.CartItemRequest;
import com.tanm.backend.dto.CartQuantityPatchRequest;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.entity.Cart;
import com.tanm.backend.entity.CartItem;
import com.tanm.backend.entity.Product;
import com.tanm.backend.enums.CartStatus;
import com.tanm.backend.enums.ProductStatus;
import com.tanm.backend.exception.BadRequestException;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.mapper.CartMapper;
import com.tanm.backend.repository.CartItemRepository;
import com.tanm.backend.repository.CartRepository;
import com.tanm.backend.repository.ProductRepository;
import com.tanm.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartDto getOrCreateUserCart(AppUser user) {
        Cart cart = getOrCreateUserCartEntity(user);
        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public CartDto getOrCreateGuestCart(String guestToken) {
        Cart cart = getOrCreateGuestCartEntity(guestToken);
        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public CartDto addItemToUserCart(AppUser user, CartItemRequest request) {
        Cart cart = getOrCreateUserCartEntity(user);
        addItemToCartEntity(cart, request);
        Cart saved = cartRepository.save(cart);
        return cartMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CartDto addItemToGuestCart(String guestToken, CartItemRequest request) {
        Cart cart = getOrCreateGuestCartEntity(guestToken);
        addItemToCartEntity(cart, request);
        Cart saved = cartRepository.save(cart);
        return cartMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CartDto patchUserCartItem(AppUser user, Long itemId, CartQuantityPatchRequest request) {
        Cart cart = getOrCreateUserCartEntity(user);
        patchCartItemEntity(cart, itemId, request.getQuantity());
        Cart saved = cartRepository.save(cart);
        return cartMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CartDto patchGuestCartItem(String guestToken, Long itemId, CartQuantityPatchRequest request) {
        Cart cart = getOrCreateGuestCartEntity(guestToken);
        patchCartItemEntity(cart, itemId, request.getQuantity());
        Cart saved = cartRepository.save(cart);
        return cartMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CartDto removeUserCartItem(AppUser user, Long itemId) {
        Cart cart = getOrCreateUserCartEntity(user);
        removeCartItemEntity(cart, itemId);
        Cart saved = cartRepository.save(cart);
        return cartMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CartDto removeGuestCartItem(String guestToken, Long itemId) {
        Cart cart = getOrCreateGuestCartEntity(guestToken);
        removeCartItemEntity(cart, itemId);
        Cart saved = cartRepository.save(cart);
        return cartMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CartDto clearUserCart(AppUser user) {
        Cart cart = getOrCreateUserCartEntity(user);
        cart.getItems().clear();
        Cart saved = cartRepository.save(cart);
        return cartMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CartDto clearGuestCart(String guestToken) {
        Cart cart = getOrCreateGuestCartEntity(guestToken);
        cart.getItems().clear();
        Cart saved = cartRepository.save(cart);
        return cartMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CartDto mergeCarts(AppUser user, String guestToken) {
        Cart userCart = getOrCreateUserCartEntity(user);
        Cart guestCart = cartRepository.findByGuestTokenAndStatusWithItems(guestToken, CartStatus.ACTIVE).orElse(null);

        if (guestCart == null || guestCart.getItems().isEmpty()) {
            return cartMapper.toDto(userCart);
        }

        for (CartItem guestItem : guestCart.getItems()) {
            Product product = guestItem.getProduct();
            int quantity = guestItem.getQuantity();

            CartItem userItem = userCart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(product.getId()))
                    .findFirst()
                    .orElse(null);

            if (userItem != null) {
                int newQty = userItem.getQuantity() + quantity;
                // Cap at stock availability during merge
                if (newQty > product.getStockQuantity()) {
                    newQty = Math.max(product.getStockQuantity(), 0);
                }
                userItem.setQuantity(newQty);
            } else {
                int newQty = quantity;
                if (newQty > product.getStockQuantity()) {
                    newQty = Math.max(product.getStockQuantity(), 0);
                }
                if (newQty > 0) {
                    CartItem newItem = CartItem.builder()
                            .cart(userCart)
                            .product(product)
                            .quantity(newQty)
                            .build();
                    userCart.getItems().add(newItem);
                }
            }
        }

        guestCart.setStatus(CartStatus.CONVERTED);
        cartRepository.save(guestCart);

        Cart savedUserCart = cartRepository.save(userCart);
        return cartMapper.toDto(savedUserCart);
    }

    private Cart getOrCreateUserCartEntity(AppUser user) {
        List<Cart> carts = cartRepository.findByUserAndStatusWithItemsList(user, CartStatus.ACTIVE);
        if (!carts.isEmpty()) {
            return carts.get(0);
        }
        Cart newCart = Cart.builder()
                .user(user)
                .status(CartStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
        return cartRepository.save(newCart);
    }

    private Cart getOrCreateGuestCartEntity(String guestToken) {
        List<Cart> carts = cartRepository.findByGuestTokenAndStatusWithItemsList(guestToken, CartStatus.ACTIVE);
        if (!carts.isEmpty()) {
            return carts.get(0);
        }
        Cart newCart = Cart.builder()
                .guestToken(guestToken)
                .status(CartStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
        return cartRepository.save(newCart);
    }

    private void addItemToCartEntity(Cart cart, CartItemRequest request) {
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int targetQty = existingItem.getQuantity() + request.getQuantity();
            validateProductForCart(request.getProductId(), targetQty);
            existingItem.setQuantity(targetQty);
        } else {
            validateProductForCart(request.getProductId(), request.getQuantity());
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
        }
    }

    private void patchCartItemEntity(Cart cart, Long itemId, int newQuantity) {
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Item does not exist in this cart"));

        validateProductForCart(item.getProduct().getId(), newQuantity);
        item.setQuantity(newQuantity);
    }

    private void removeCartItemEntity(Cart cart, Long itemId) {
        boolean removed = cart.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) {
            throw new BadRequestException("Item does not exist in this cart");
        }
    }

    private void validateProductForCart(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (product.isDeleted()) {
            throw new BadRequestException("Cannot add discontinued product to cart");
        }
        if (!product.isActive() || product.getStatus() != ProductStatus.PUBLISHED) {
            throw new BadRequestException("Cannot add unavailable product to cart");
        }
        if (product.getStockQuantity() <= 0) {
            throw new BadRequestException("Product is out of stock");
        }
        if (product.getStockQuantity() < quantity) {
            throw new BadRequestException("Cannot request " + quantity + " items; only " + product.getStockQuantity() + " remaining");
        }
    }
}
