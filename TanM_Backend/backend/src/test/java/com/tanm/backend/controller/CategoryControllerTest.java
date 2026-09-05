package com.tanm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanm.backend.dto.CategoryCreateRequest;
import com.tanm.backend.dto.CategoryDto;
import com.tanm.backend.service.CategoryService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CategoryController.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = {com.tanm.backend.config.SecurityConfig.class, com.tanm.backend.config.JwtAuthenticationFilter.class}
        )
)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryDto categoryDto;
    private CategoryCreateRequest request;

    @BeforeEach
    void setUp() {
        categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Electronics")
                .description("Devices and gadgets")
                .slug("electronics")
                .imageUrl("http://example.com/image.jpg")
                .displayOrder(5)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        request = CategoryCreateRequest.builder()
                .name("Electronics")
                .description("Devices and gadgets")
                .imageUrl("http://example.com/image.jpg")
                .displayOrder(5)
                .build();
    }

    @Test
    void createCategory_shouldReturnCreatedCategory() throws Exception {
        Mockito.when(categoryService.createCategory(any(CategoryCreateRequest.class)))
                .thenReturn(categoryDto);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.description").value("Devices and gadgets"))
                .andExpect(jsonPath("$.slug").value("electronics"))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/image.jpg"))
                .andExpect(jsonPath("$.displayOrder").value(5))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.deleted").doesNotExist());
    }

    @Test
    void getCategoryById_shouldReturnCategory() throws Exception {
        Mockito.when(categoryService.getCategoryById(1L))
                .thenReturn(categoryDto);

        mockMvc.perform(get("/api/categories/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.slug").value("electronics"))
                .andExpect(jsonPath("$.deleted").doesNotExist());
    }

    @Test
    void getCategoryBySlug_shouldReturnCategory() throws Exception {
        Mockito.when(categoryService.getCategoryBySlug("electronics"))
                .thenReturn(categoryDto);

        mockMvc.perform(get("/api/categories/{slug}", "electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.slug").value("electronics"))
                .andExpect(jsonPath("$.deleted").doesNotExist());
    }

    @Test
    void getAllCategories_shouldReturnList() throws Exception {
        org.springframework.data.domain.Page<CategoryDto> page = new org.springframework.data.domain.PageImpl<>(Arrays.asList(categoryDto));
        Mockito.when(categoryService.getAllCategories(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Electronics"));
    }

    @Test
    void updateCategory_shouldReturnUpdatedCategory() throws Exception {
        Mockito.when(categoryService.updateCategory(eq(1L), any(CategoryCreateRequest.class)))
                .thenReturn(categoryDto);

        mockMvc.perform(put("/api/categories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.deleted").doesNotExist());
    }

    @Test
    void deleteCategory_shouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/categories/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
