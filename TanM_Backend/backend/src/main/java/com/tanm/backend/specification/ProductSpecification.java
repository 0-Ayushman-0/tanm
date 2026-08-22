package com.tanm.backend.specification;

import com.tanm.backend.dto.ProductFilterRequest;
import com.tanm.backend.entity.Product;
import com.tanm.backend.enums.ProductStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filter(ProductFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always enforce non-deleted published products for catalog filters
            predicates.add(cb.equal(root.get("isDeleted"), false));
            predicates.add(cb.equal(root.get("status"), ProductStatus.PUBLISHED));

            if (filter != null) {
                if (filter.getCategoryId() != null) {
                    predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
                }

                if (filter.getMinPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
                }

                if (filter.getMaxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
                }

                if (filter.getLeatherType() != null && !filter.getLeatherType().isBlank()) {
                    predicates.add(cb.like(cb.lower(root.get("leatherType")), "%" + filter.getLeatherType().toLowerCase().trim() + "%"));
                }

                if (filter.getColor() != null && !filter.getColor().isBlank()) {
                    predicates.add(cb.like(cb.lower(root.get("color")), "%" + filter.getColor().toLowerCase().trim() + "%"));
                }

                if (Boolean.TRUE.equals(filter.getInStockOnly())) {
                    predicates.add(cb.greaterThan(root.get("stockQuantity"), 0));
                }

                if (filter.getIsFeatured() != null) {
                    predicates.add(cb.equal(root.get("isFeatured"), filter.getIsFeatured()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
