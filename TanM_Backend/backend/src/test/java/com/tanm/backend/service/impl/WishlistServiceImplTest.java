package com.tanm.backend.service.impl;

import com.tanm.backend.dto.ProductDto;
import com.tanm.backend.dto.WishlistDto;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.entity.Product;
import com.tanm.backend.entity.Wishlist;
import com.tanm.backend.entity.WishlistItem;
import com.tanm.backend.mapper.ProductMapper;
import com.tanm.backend.repository.ProductRepository;
import com.tanm.backend.repository.WishlistItemRepository;
import com.tanm.backend.repository.WishlistRepository;
import com.tanm.backend.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class WishlistServiceImplTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CartService cartService;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private AppUser user;
    private Product product;
    private Wishlist wishlist;

    @BeforeEach
    void setUp() {
        user = AppUser.builder().email("shopper@tanm.com").build();
        user.setId(7L);

        product = Product.builder().name("Messenger Bag").sku("MSG-001").build();
        product.setId(14L);

        wishlist = Wishlist.builder().user(user).items(new ArrayList<>()).build();
        wishlist.setId(100L);
    }

    @Test
    void addToWishlist_shouldAddProductIfNotPresent() {
        Mockito.when(wishlistRepository.findByUser(user))
                .thenReturn(Optional.of(wishlist));
        Mockito.when(productRepository.findByIdAndIsDeletedFalse(14L))
                .thenReturn(Optional.of(product));
        Mockito.when(wishlistItemRepository.existsByWishlistAndProduct(wishlist, product))
                .thenReturn(false);
        Mockito.when(wishlistRepository.save(any(Wishlist.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(productMapper.toDto(product))
                .thenReturn(ProductDto.builder().id(14L).name("Messenger Bag").sku("MSG-001").build());

        WishlistDto dto = wishlistService.addToWishlist(user, 14L);

        assertThat(dto.getTotalItems()).isEqualTo(1);
        assertThat(dto.getItems().get(0).getProduct().getSku()).isEqualTo("MSG-001");
    }

    @Test
    void moveToCart_shouldRemoveFromWishlistAndInvokeCartService() {
        WishlistItem item = WishlistItem.builder().wishlist(wishlist).product(product).build();
        wishlist.getItems().add(item);

        Mockito.when(wishlistRepository.findByUser(user))
                .thenReturn(Optional.of(wishlist));
        Mockito.when(productRepository.findByIdAndIsDeletedFalse(14L))
                .thenReturn(Optional.of(product));
        Mockito.when(wishlistItemRepository.findByWishlistAndProduct(wishlist, product))
                .thenReturn(Optional.of(item));

        WishlistDto dto = wishlistService.moveToCart(user, 14L, 2);

        assertThat(dto.getTotalItems()).isEqualTo(0);
        Mockito.verify(cartService).addItemToUserCart(eq(user), any(com.tanm.backend.dto.CartItemRequest.class));
    }
}
