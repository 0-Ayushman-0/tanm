package com.tanm.backend.entity;

import com.tanm.backend.common.BaseEntity;
import com.tanm.backend.enums.CmsStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cms_static_pages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsStaticPage extends BaseEntity {

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "slug", nullable = false, unique = true, length = 150)
    private String slug;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "seo_metadata_id")
    private SeoMetadata seoMetadata;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CmsStatus status = CmsStatus.DRAFT;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "publish_at")
    private LocalDateTime publishAt;

    @Column(name = "unpublish_at")
    private LocalDateTime unpublishAt;

    @Column(name = "current_version_number")
    @Builder.Default
    private int currentVersionNumber = 1;
}
