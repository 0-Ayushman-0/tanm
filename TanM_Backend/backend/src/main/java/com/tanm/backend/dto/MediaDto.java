package com.tanm.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaDto {
    private Long id;
    private String fileName;
    private String storageKey;
    private String url;
    private String thumbnailUrl;
    private String mimeType;
    private Integer width;
    private Integer height;
    private Long size;
    private String altText;
    private String folder;
    private String uploadedBy;
    private LocalDateTime createdAt;
}
