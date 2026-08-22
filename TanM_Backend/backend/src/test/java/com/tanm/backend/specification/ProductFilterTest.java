package com.tanm.backend.specification;

import com.tanm.backend.dto.ProductFilterRequest;
import com.tanm.backend.entity.Category;
import com.tanm.backend.entity.Product;
import com.tanm.backend.enums.ProductStatus;
import com.tanm.backend.repository.CategoryRepository;
import com.tanm.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("dev")
class ProductFilterTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        category = categoryRepository.save(Category.builder()
                .name("Filter Category")
                .slug("filter-cat-" + System.currentTimeMillis())
                .build());

        productRepository.save(Product.builder()
                .name("Tan Full Grain Duffle Bag")
                .sku("DUFFLE-TAN-01")
                .slug("tan-duffle-bag-" + System.currentTimeMillis())
                .price(BigDecimal.valueOf(8500.00))
                .leatherType("Full Grain")
                .color("Tan")
                .stockQuantity(15)
                .isFeatured(true)
                .status(ProductStatus.PUBLISHED)
                .category(category)
                .build());

        productRepository.save(Product.builder()
                .name("Black Top Grain Wallet")
                .sku("WALLET-BLK-01")
                .slug("black-wallet-" + System.currentTimeMillis())
                .price(BigDecimal.valueOf(1999.00))
                .leatherType("Top Grain")
                .color("Black")
                .stockQuantity(0) // Out of stock
                .isFeatured(false)
                .status(ProductStatus.PUBLISHED)
                .category(category)
                .build());
    }

    @Test
    void filter_shouldFilterByPriceRangeAndStock() {
        ProductFilterRequest request = ProductFilterRequest.builder()
                .minPrice(BigDecimal.valueOf(5000.00))
                .maxPrice(BigDecimal.valueOf(10000.00))
                .inStockOnly(true)
                .build();

        Page<Product> page = productRepository.findAll(ProductSpecification.filter(request), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getSku()).isEqualTo("DUFFLE-TAN-01");
    }

    @Test
    void filter_shouldFilterByLeatherTypeAndColor() {
        ProductFilterRequest request = ProductFilterRequest.builder()
                .leatherType("Full Grain")
                .color("Tan")
                .build();

        Page<Product> page = productRepository.findAll(ProductSpecification.filter(request), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).contains("Full Grain");
    }
}
