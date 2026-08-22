package com.tanm.backend.dto;

import com.tanm.backend.enums.CmsStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsTestimonialDto {
    private Long id;

    @NotBlank(message = "Customer name is required")
    private String customerName;
    private String customerTitle;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private int rating;

    @NotBlank(message = "Comment is required")
    private String comment;

    private MediaDto avatar;
    private Long avatarId;
    private int displayOrder;
    private CmsStatus status;
}
