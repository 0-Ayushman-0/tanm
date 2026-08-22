package com.tanm.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeoMetadataDto {
    private Long id;
    private String title;
    private String description;
    private String keywords;
    private String canonicalUrl;
    private String ogTitle;
    private String ogDescription;
    private MediaDto ogImage;
    private Long ogImageId;
    private String robots;
}
