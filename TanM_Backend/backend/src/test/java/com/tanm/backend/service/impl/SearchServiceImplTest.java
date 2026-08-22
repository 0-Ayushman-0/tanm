package com.tanm.backend.service.impl;

import com.tanm.backend.dto.CategoryDto;
import com.tanm.backend.dto.CollectionDto;
import com.tanm.backend.dto.GlobalSearchResponseDto;
import com.tanm.backend.dto.ProductDto;
import com.tanm.backend.entity.Category;
import com.tanm.backend.entity.Collection;
import com.tanm.backend.entity.Product;
import com.tanm.backend.mapper.CategoryMapper;
import com.tanm.backend.mapper.CollectionMapper;
import com.tanm.backend.mapper.ProductMapper;
import com.tanm.backend.repository.CategoryRepository;
import com.tanm.backend.repository.CollectionRepository;
import com.tanm.backend.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CollectionMapper collectionMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private SearchServiceImpl searchService;

    @Test
    void globalSearch_shouldAggregateProductsCollectionsAndCategories() {
        Product p = Product.builder().name("Leather Wallet").sku("WAL-001").build();
        Collection col = Collection.builder().name("Summer Collection").build();
        Category cat = Category.builder().name("Wallets").build();

        ProductDto pDto = ProductDto.builder().name("Leather Wallet").sku("WAL-001").build();
        CollectionDto colDto = CollectionDto.builder().name("Summer Collection").build();
        CategoryDto catDto = CategoryDto.builder().name("Wallets").build();

        Mockito.when(productRepository.searchProductsQuick(eq("leather"), any(Pageable.class))).thenReturn(List.of(p));
        Mockito.when(collectionRepository.searchCollectionsQuick(eq("leather"), any(Pageable.class))).thenReturn(List.of(col));
        Mockito.when(categoryRepository.searchCategoriesQuick(eq("leather"), any(Pageable.class))).thenReturn(List.of(cat));

        Mockito.when(productMapper.toDto(p)).thenReturn(pDto);
        Mockito.when(collectionMapper.toDto(col)).thenReturn(colDto);
        Mockito.when(categoryMapper.toDto(cat)).thenReturn(catDto);

        GlobalSearchResponseDto response = searchService.globalSearch("leather", 5);

        assertThat(response.getQuery()).isEqualTo("leather");
        assertThat(response.getProducts()).hasSize(1);
        assertThat(response.getProducts().get(0).getSku()).isEqualTo("WAL-001");
        assertThat(response.getCollections()).hasSize(1);
        assertThat(response.getCategories()).hasSize(1);
    }
}
