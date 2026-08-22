package com.tanm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanm.backend.dto.CollectionCreateRequest;
import com.tanm.backend.dto.CollectionDetailDto;
import com.tanm.backend.dto.CollectionDto;
import com.tanm.backend.dto.ProductDto;
import com.tanm.backend.service.CollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CollectionController.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = {com.tanm.backend.config.SecurityConfig.class, com.tanm.backend.config.JwtAuthenticationFilter.class}
        )
)
class CollectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CollectionService collectionService;

    @Autowired
    private ObjectMapper objectMapper;

    private CollectionDto collectionDto;
    private CollectionDetailDto collectionDetailDto;
    private CollectionCreateRequest request;

    @BeforeEach
    void setUp() {
        collectionDto = CollectionDto.builder()
                .id(1L)
                .name("Signature Series")
                .description("Luxury standard bag drops")
                .slug("signature-series")
                .imageUrl("http://example.com/collection.jpg")
                .displayOrder(10)
                .isFeatured(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ProductDto productDto = ProductDto.builder()
                .id(100L)
                .name("Luxury Tote")
                .sku("BG-TOTE-100")
                .slug("luxury-tote")
                .build();

        collectionDetailDto = CollectionDetailDto.builder()
                .id(1L)
                .name("Signature Series")
                .description("Luxury standard bag drops")
                .slug("signature-series")
                .imageUrl("http://example.com/collection.jpg")
                .displayOrder(10)
                .isFeatured(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .products(Collections.singletonList(productDto))
                .build();

        request = CollectionCreateRequest.builder()
                .name("Signature Series")
                .description("Luxury standard bag drops")
                .imageUrl("http://example.com/collection.jpg")
                .displayOrder(10)
                .isFeatured(true)
                .build();
    }

    @Test
    void createCollection_shouldReturnCreatedCollection() throws Exception {
        Mockito.when(collectionService.createCollection(any(CollectionCreateRequest.class)))
                .thenReturn(collectionDto);

        mockMvc.perform(post("/api/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Signature Series"))
                .andExpect(jsonPath("$.slug").value("signature-series"))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/collection.jpg"))
                .andExpect(jsonPath("$.displayOrder").value(10))
                .andExpect(jsonPath("$.featured").value(true))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getCollectionById_shouldReturnCollectionDetail() throws Exception {
        Mockito.when(collectionService.getCollectionById(1L))
                .thenReturn(collectionDetailDto);

        mockMvc.perform(get("/api/collections/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.slug").value("signature-series"))
                .andExpect(jsonPath("$.products[0].id").value(100L))
                .andExpect(jsonPath("$.products[0].name").value("Luxury Tote"));
    }

    @Test
    void getCollectionBySlug_shouldReturnCollectionDetail() throws Exception {
        Mockito.when(collectionService.getCollectionBySlug("signature-series"))
                .thenReturn(collectionDetailDto);

        mockMvc.perform(get("/api/collections/{slug}", "signature-series"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.slug").value("signature-series"))
                .andExpect(jsonPath("$.products[0].id").value(100L));
    }

    @Test
    void getAllCollections_shouldReturnList() throws Exception {
        org.springframework.data.domain.Page<CollectionDto> page = new org.springframework.data.domain.PageImpl<>(Arrays.asList(collectionDto));
        Mockito.when(collectionService.getAllCollections(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/collections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Signature Series"));
    }

    @Test
    void updateCollection_shouldReturnUpdatedCollection() throws Exception {
        Mockito.when(collectionService.updateCollection(eq(1L), any(CollectionCreateRequest.class)))
                .thenReturn(collectionDto);

        mockMvc.perform(put("/api/collections/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Signature Series"));
    }

    @Test
    void deleteCollection_shouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(collectionService).deleteCollection(1L);

        mockMvc.perform(delete("/api/collections/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void addProductToCollection_shouldReturnOk() throws Exception {
        Mockito.doNothing().when(collectionService).addProductToCollection(1L, 100L);

        mockMvc.perform(post("/api/collections/{id}/products/{productId}", 1L, 100L))
                .andExpect(status().isOk());
    }

    @Test
    void removeProductFromCollection_shouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(collectionService).removeProductFromCollection(1L, 100L);

        mockMvc.perform(delete("/api/collections/{id}/products/{productId}", 1L, 100L))
                .andExpect(status().isNoContent());
    }
}
