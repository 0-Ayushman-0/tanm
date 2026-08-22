package com.tanm.backend.service.impl;

import com.tanm.backend.dto.CartDto;
import com.tanm.backend.dto.CartItemRequest;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.entity.Cart;
import com.tanm.backend.entity.CartItem;
import com.tanm.backend.entity.Product;
import com.tanm.backend.enums.CartStatus;
import com.tanm.backend.enums.ProductStatus;
import com.tanm.backend.exception.BadRequestException;
import com.tanm.backend.mapper.CartMapper;
import com.tanm.backend.repository.CartItemRepository;
import com.tanm.backend.repository.CartRepository;
import com.tanm.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private AppUser user;
    private Product product;
    private Cart userCart;
    private Cart guestCart;

    @BeforeEach
    void setUp() {
        user = AppUser.builder().email("jane@example.com").build();
        user.setId(15L);

        product = Product.builder()
                .name("Leather Belt")
                .price(java.math.BigDecimal.valueOf(80.00))
                .stockQuantity(10)
                .status(ProductStatus.PUBLISHED)
                .build();
        product.setId(100L);
        product.setActive(true);
        product.setDeleted(false);

        userCart = Cart.builder()
                .user(user)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();
        userCart.setId(1L);

        guestCart = Cart.builder()
                .guestToken("guest-token-abc")
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();
        guestCart.setId(2L);
    }

    @Test
    void addItemToUserCart_exceedingStock_shouldThrowBadRequestException() {
        // Arrange
        CartItemRequest request = CartItemRequest.builder()
                .productId(100L)
                .quantity(11) // Stock is 10, requests 11
                .build();

        Mockito.when(cartRepository.findByUserAndStatusWithItems(eq(user), eq(CartStatus.ACTIVE)))
                .thenReturn(Optional.of(userCart));
        Mockito.when(productRepository.findById(100L))
                .thenReturn(Optional.of(product));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                cartService.addItemToUserCart(user, request)
        );
        assertTrue(ex.getMessage().contains("only 10 remaining"));
    }

    @Test
    void mergeCarts_shouldSumQuantitiesAndCapAtStockAvailability() {
        // Arrange
        // User Cart has 2 Leather Belts
        userCart.getItems().add(CartItem.builder().cart(userCart).product(product).quantity(2).build());

        // Guest Cart has 9 Leather Belts
        guestCart.getItems().add(CartItem.builder().cart(guestCart).product(product).quantity(9).build());

        Mockito.when(cartRepository.findByUserAndStatusWithItems(eq(user), eq(CartStatus.ACTIVE)))
                .thenReturn(Optional.of(userCart));
        Mockito.when(cartRepository.findByGuestTokenAndStatusWithItems(eq("guest-token-abc"), eq(CartStatus.ACTIVE)))
                .thenReturn(Optional.of(guestCart));

        Mockito.when(cartRepository.save(eq(guestCart))).thenReturn(guestCart);
        Mockito.when(cartRepository.save(eq(userCart))).thenReturn(userCart);
        Mockito.when(cartMapper.toDto(any(Cart.class))).thenReturn(CartDto.builder().build());

        // Act
        cartService.mergeCarts(user, "guest-token-abc");

        // Assert
        assertEquals(CartStatus.CONVERTED, guestCart.getStatus()); // Guest cart converted
        assertEquals(1, userCart.getItems().size());
        assertEquals(10, userCart.getItems().get(0).getQuantity()); // 2 + 9 = 11, capped at stock availability of 10!

        Mockito.verify(cartRepository).save(guestCart);
        Mockito.verify(cartRepository).save(userCart);
    }
}
