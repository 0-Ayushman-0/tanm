package com.tanm.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRatingSummaryDto {
    private Long productId;
    private double averageRating;
    private long totalReviews;
    private long star5Count;
    private long star4Count;
    private long star3Count;
    private long star2Count;
    private long star1Count;
}
