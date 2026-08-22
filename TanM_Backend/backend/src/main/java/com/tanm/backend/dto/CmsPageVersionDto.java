package com.tanm.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsPageVersionDto {
    private Long id;
    private Long pageId;
    private int versionNumber;
    private String title;
    private String content;
    private String seoMetadataJson;
    private String publishedBy;
    private LocalDateTime publishedAt;
}
