package com.tanm.backend.dto;

import com.tanm.backend.enums.BannerType;
import com.tanm.backend.enums.CmsStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsBannerDto {
    private Long id;

    @NotNull(message = "Banner type is required")
    private BannerType bannerType;
    private String title;
    private String subtitle;
    private MediaDto desktopImage;
    private Long desktopImageId;
    private MediaDto mobileImage;
    private Long mobileImageId;
    private String buttonText;
    private String buttonUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int priority;
    private CmsStatus status;
}
