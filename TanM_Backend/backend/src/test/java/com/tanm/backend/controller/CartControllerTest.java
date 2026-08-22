package com.tanm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanm.backend.dto.*;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.enums.CartStatus;
import com.tanm.backend.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CartController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = {com.tanm.backend.config.SecurityConfig.class, com.tanm.backend.config.JwtAuthenticationFilter.class}
        )
)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private CartDto cartDto;
    private CartItemRequest itemRequest;
    private CartQuantityPatchRequest patchRequest;
    private CartMergeRequest mergeRequest;

    @BeforeEach
    void setUp() {
        CartItemDto itemDto = CartItemDto.builder()
                .id(10L)
                .productId(100L)
                .productName("Leather Wallet")
                .slug("leather-wallet")
                .price(BigDecimal.valueOf(50.00))
                .quantity(2)
                .subtotal(BigDecimal.valueOf(100.00))
                .isAvailable(true)
                .build();

        cartDto = CartDto.builder()
                .id(1L)
                .guestToken("guest-token-123")
                .status(CartStatus.ACTIVE)
                .items(Collections.singletonList(itemDto))
                .totalQuantity(2)
                .totalPrice(BigDecimal.valueOf(100.00))
                .build();

        itemRequest = CartItemRequest.builder()
                .productId(100L)
                .quantity(2)
                .build();

        patchRequest = CartQuantityPatchRequest.builder()
                .quantity(5)
                .build();

        mergeRequest = CartMergeRequest.builder()
                .guestToken("guest-token-123")
                .build();
    }

    @Test
    void getCart_shouldReturnCart() throws Exception {
        Mockito.when(cartService.getOrCreateGuestCart("guest-token-123"))
                .thenReturn(cartDto);

        mockMvc.perform(get("/api/cart")
                        .header("Guest-Token", "guest-token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.items[0].productName").value("Leather Wallet"))
                .andExpect(jsonPath("$.totalPrice").value(100.00));
    }

    @Test
    void getCart_withoutIdentifiers_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemToCart_shouldReturnUpdatedCart() throws Exception {
        Mockito.when(cartService.addItemToGuestCart(eq("guest-token-123"), any(CartItemRequest.class)))
                .thenReturn(cartDto);

        mockMvc.perform(post("/api/cart/items")
                        .header("Guest-Token", "guest-token-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void patchCartItem_shouldReturnUpdatedCart() throws Exception {
        Mockito.when(cartService.patchGuestCartItem(eq("guest-token-123"), eq(10L), any(CartQuantityPatchRequest.class)))
                .thenReturn(cartDto);

        mockMvc.perform(patch("/api/cart/items/{itemId}", 10L)
                        .header("Guest-Token", "guest-token-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void removeCartItem_shouldReturnUpdatedCart() throws Exception {
        Mockito.when(cartService.removeGuestCartItem(eq("guest-token-123"), eq(10L)))
                .thenReturn(cartDto);

        mockMvc.perform(delete("/api/cart/items/{itemId}", 10L)
                        .header("Guest-Token", "guest-token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void clearCart_shouldReturnUpdatedCart() throws Exception {
        Mockito.when(cartService.clearGuestCart("guest-token-123"))
                .thenReturn(cartDto);

        mockMvc.perform(delete("/api/cart")
                        .header("Guest-Token", "guest-token-123"))
                .andExpect(status().isOk());
    }

    @Test
    void mergeCart_shouldReturnMergedCart() throws Exception {
        // Mock authentication context for authenticated merge
        AppUser principal = AppUser.builder().email("john@example.com").build();
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        java.util.Collections.emptyList()
                );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

        Mockito.when(cartService.mergeCarts(any(), eq("guest-token-123")))
                .thenReturn(cartDto);

        try {
            mockMvc.perform(post("/api/cart/merge")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(mergeRequest)))
                    .andExpect(status().isOk());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}
