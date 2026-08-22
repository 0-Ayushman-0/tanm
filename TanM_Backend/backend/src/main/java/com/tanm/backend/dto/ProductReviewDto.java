package com.tanm.backend.dto;

import com.tanm.backend.enums.ReviewStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReviewDto {
    private Long id;
    private Long productId;
    private String productName;
    private Long userId;
    private String userName;
    private int rating;
    private String title;
    private String comment;
    private boolean isVerifiedPurchase;
    private ReviewStatus status;
    private int helpfulCount;
    private List<MediaDto> images;
    private LocalDateTime createdAt;
}
