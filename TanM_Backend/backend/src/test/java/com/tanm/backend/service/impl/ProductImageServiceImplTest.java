package com.tanm.backend.service.impl;

import com.tanm.backend.dto.ProductImageAddRequest;
import com.tanm.backend.dto.ProductImageDto;
import com.tanm.backend.entity.Product;
import com.tanm.backend.entity.ProductImage;
import com.tanm.backend.mapper.ProductMapper;
import com.tanm.backend.repository.ProductImageRepository;
import com.tanm.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductImageServiceImpl productImageService;

    private Product product;
    private ProductImage primaryImage;
    private ProductImage secondaryImage;

    @BeforeEach
    void setUp() {
        product = Product.builder().build();
        product.setId(100L);

        primaryImage = ProductImage.builder()
                .product(product)
                .imageUrl("http://example.com/primary.jpg")
                .isPrimary(true)
                .build();
        primaryImage.setId(1L);
        primaryImage.setDeleted(false);

        secondaryImage = ProductImage.builder()
                .product(product)
                .imageUrl("http://example.com/secondary.jpg")
                .isPrimary(false)
                .build();
        secondaryImage.setId(2L);
        secondaryImage.setDeleted(false);
    }

    @Test
    void addProductImage_ifFirstImage_shouldForcePrimary() {
        // Arrange
        ProductImageAddRequest addRequest = ProductImageAddRequest.builder()
                .imageUrl("http://example.com/first.jpg")
                .isPrimary(false) // Requests non-primary
                .build();

        Mockito.when(productRepository.findByIdAndIsDeletedFalse(100L))
                .thenReturn(Optional.of(product));
        Mockito.when(productImageRepository.countByProductIdAndIsDeletedFalse(100L))
                .thenReturn(0L); // First image
        Mockito.when(productImageRepository.save(any(ProductImage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(productMapper.toProductImageDto(any(ProductImage.class)))
                .thenReturn(ProductImageDto.builder().isPrimary(true).build());

        // Act
        ProductImageDto result = productImageService.addProductImage(100L, addRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isPrimary()); // Forced to primary because activeCount = 0
    }

    @Test
    void removeProductImage_ifWasPrimary_shouldPromoteNextImage() {
        // Arrange
        Mockito.when(productRepository.findByIdAndIsDeletedFalse(100L))
                .thenReturn(Optional.of(product));
        Mockito.when(productImageRepository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(primaryImage));

        // When primary is removed, remaining active images are loaded
        Mockito.when(productImageRepository.findAllByProductIdAndIsDeletedFalseOrderByDisplayOrderAscIdAsc(100L))
                .thenReturn(Collections.singletonList(secondaryImage));

        // Act
        productImageService.removeProductImage(100L, 1L);

        // Assert
        assertTrue(primaryImage.isDeleted());
        assertFalse(primaryImage.isPrimary());
        assertTrue(secondaryImage.isPrimary()); // Secondary promoted to primary!

        Mockito.verify(productImageRepository).save(primaryImage);
        Mockito.verify(productImageRepository).save(secondaryImage);
    }

    @Test
    void setPrimaryImage_shouldResetOldPrimaryAndSetNew() {
        // Arrange
        Mockito.when(productRepository.findByIdAndIsDeletedFalse(100L))
                .thenReturn(Optional.of(product));
        Mockito.when(productImageRepository.findByIdAndIsDeletedFalse(2L))
                .thenReturn(Optional.of(secondaryImage));
        Mockito.when(productImageRepository.save(any(ProductImage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        productImageService.setPrimaryImage(100L, 2L);

        // Assert
        assertTrue(secondaryImage.isPrimary());
        Mockito.verify(productImageRepository).resetPrimaryImageForProduct(100L); // Old primary reset
        Mockito.verify(productImageRepository).save(secondaryImage);
    }
}
