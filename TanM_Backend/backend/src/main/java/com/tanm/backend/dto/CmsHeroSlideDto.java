package com.tanm.backend.dto;

import com.tanm.backend.enums.CmsStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsHeroSlideDto {
    private Long id;

    @NotBlank(message = "Hero title is required")
    private String title;
    private String subtitle;
    private String description;
    private String primaryCtaText;
    private String primaryCtaUrl;
    private String secondaryCtaText;
    private String secondaryCtaUrl;
    private MediaDto backgroundImage;
    private Long backgroundImageId;
    private MediaDto mobileImage;
    private Long mobileImageId;
    private String videoUrl;
    private Double overlayOpacity;
    private int sortOrder;
    private CmsStatus status;
    private LocalDateTime publishAt;
    private LocalDateTime unpublishAt;
}
