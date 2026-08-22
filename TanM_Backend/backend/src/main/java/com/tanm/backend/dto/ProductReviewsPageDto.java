package com.tanm.backend.dto;

import lombok.*;
import org.springframework.data.domain.Page;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReviewsPageDto {
    private ProductRatingSummaryDto summary;
    private Page<ProductReviewDto> reviews;
}
