package com.tanm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanm.backend.dto.CategoryDto;
import com.tanm.backend.dto.ProductCreateRequest;
import com.tanm.backend.dto.ProductDto;
import com.tanm.backend.enums.ProductStatus;
import com.tanm.backend.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ProductController.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = {com.tanm.backend.config.SecurityConfig.class, com.tanm.backend.config.JwtAuthenticationFilter.class}
        )
)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private com.tanm.backend.service.ProductImageService productImageService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDto productDto;
    private ProductCreateRequest request;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Leather Bags")
                .slug("leather-bags")
                .description("Handmade leather bags")
                .isActive(true)
                .build();

        productDto = ProductDto.builder()
                .id(1L)
                .name("Classic Handbag")
                .slug("classic-handbag")
                .sku("BG-CLASSIC-01")
                .shortDescription("A classic handbag")
                .description("Detailed description")
                .price(new BigDecimal("199.99"))
                .stockQuantity(10)
                .mainImageUrl("http://example.com/bag.jpg")
                .leatherType("Full Grain")
                .color("Tan")
                .dimensions("12x8x4")
                .isFeatured(true)
                .status(ProductStatus.PUBLISHED)
                .category(categoryDto)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        request = ProductCreateRequest.builder()
                .name("Classic Handbag")
                .sku("BG-CLASSIC-01")
                .shortDescription("A classic handbag")
                .description("Detailed description")
                .price(new BigDecimal("199.99"))
                .stockQuantity(10)
                .leatherType("Full Grain")
                .color("Tan")
                .dimensions("12x8x4")
                .isFeatured(true)
                .status(ProductStatus.PUBLISHED)
                .categoryId(1L)
                .build();
    }

    @Test
    void createProduct_shouldReturnCreatedProduct() throws Exception {
        Mockito.when(productService.createProduct(any(ProductCreateRequest.class)))
                .thenReturn(productDto);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Classic Handbag"))
                .andExpect(jsonPath("$.sku").value("BG-CLASSIC-01"))
                .andExpect(jsonPath("$.price").value(199.99))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.category.name").value("Leather Bags"))
                .andExpect(jsonPath("$.deleted").doesNotExist());
    }

    @Test
    void getProductById_shouldReturnProduct() throws Exception {
        Mockito.when(productService.getProductById(1L))
                .thenReturn(productDto);

        mockMvc.perform(get("/api/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Classic Handbag"))
                .andExpect(jsonPath("$.deleted").doesNotExist());
    }

    @Test
    void getProductBySlug_shouldReturnProduct() throws Exception {
        Mockito.when(productService.getProductBySlug("classic-handbag"))
                .thenReturn(productDto);

        mockMvc.perform(get("/api/products/{slug}", "classic-handbag"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.slug").value("classic-handbag"))
                .andExpect(jsonPath("$.deleted").doesNotExist());
    }

    @Test
    void getProductsByCategory_shouldReturnList() throws Exception {
        org.springframework.data.domain.Page<ProductDto> page = new org.springframework.data.domain.PageImpl<>(Arrays.asList(productDto));
        Mockito.when(productService.getProductsByCategory(eq(1L), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/products/category/{categoryId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Classic Handbag"));
    }

    @Test
    void getAllProducts_shouldReturnList() throws Exception {
        org.springframework.data.domain.Page<ProductDto> page = new org.springframework.data.domain.PageImpl<>(Arrays.asList(productDto));
        Mockito.when(productService.getAllProducts(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Classic Handbag"));
    }

    @Test
    void updateProduct_shouldReturnUpdatedProduct() throws Exception {
        Mockito.when(productService.updateProduct(eq(1L), any(ProductCreateRequest.class)))
                .thenReturn(productDto);

        mockMvc.perform(put("/api/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Classic Handbag"));
    }

    @Test
    void deleteProduct_shouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void addProductImage_shouldReturnCreatedImage() throws Exception {
        com.tanm.backend.dto.ProductImageDto imgDto = com.tanm.backend.dto.ProductImageDto.builder()
                .id(1L)
                .imageUrl("http://example.com/front.png")
                .publicId("cloudinary_id_123")
                .altText("Front View")
                .displayOrder(1)
                .isPrimary(true)
                .build();

        com.tanm.backend.dto.ProductImageAddRequest addRequest = com.tanm.backend.dto.ProductImageAddRequest.builder()
                .imageUrl("http://example.com/front.png")
                .publicId("cloudinary_id_123")
                .altText("Front View")
                .displayOrder(1)
                .isPrimary(true)
                .build();

        Mockito.when(productImageService.addProductImage(eq(1L), any(com.tanm.backend.dto.ProductImageAddRequest.class)))
                .thenReturn(imgDto);

        mockMvc.perform(post("/api/products/{id}/images", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/front.png"))
                .andExpect(jsonPath("$.publicId").value("cloudinary_id_123"))
                .andExpect(jsonPath("$.isPrimary").value(true));
    }

    @Test
    void removeProductImage_shouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(productImageService).removeProductImage(1L, 2L);

        mockMvc.perform(delete("/api/products/{id}/images/{imageId}", 1L, 2L))
                .andExpect(status().isNoContent());
    }

    @Test
    void reorderProductImages_shouldReturnOk() throws Exception {
        com.tanm.backend.dto.ProductImageReorderRequest reorderReq = com.tanm.backend.dto.ProductImageReorderRequest.builder()
                .imageOrders(Arrays.asList(
                        new com.tanm.backend.dto.ProductImageReorderRequest.ImageOrderPair(2L, 2),
                        new com.tanm.backend.dto.ProductImageReorderRequest.ImageOrderPair(3L, 1)
                ))
                .build();

        Mockito.doNothing().when(productImageService).reorderProductImages(eq(1L), any(com.tanm.backend.dto.ProductImageReorderRequest.class));

        mockMvc.perform(patch("/api/products/{id}/images/order", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reorderReq)))
                .andExpect(status().isOk());
    }

    @Test
    void setPrimaryImage_shouldReturnUpdatedImage() throws Exception {
        com.tanm.backend.dto.ProductImageDto imgDto = com.tanm.backend.dto.ProductImageDto.builder()
                .id(2L)
                .imageUrl("http://example.com/back.png")
                .publicId("cloudinary_id_456")
                .altText("Back View")
                .displayOrder(2)
                .isPrimary(true)
                .build();

        Mockito.when(productImageService.setPrimaryImage(1L, 2L))
                .thenReturn(imgDto);

        mockMvc.perform(put("/api/products/{id}/images/{imageId}/primary", 1L, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.publicId").value("cloudinary_id_456"))
                .andExpect(jsonPath("$.isPrimary").value(true));
    }
}
