package com.tanm.backend.dto;

import com.tanm.backend.enums.CmsStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsStaticPageDto {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Slug is required")
    private String slug;

    private String content;
    private SeoMetadataDto seoMetadata;
    private CmsStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime publishAt;
    private LocalDateTime unpublishAt;
    private int currentVersionNumber;
}
