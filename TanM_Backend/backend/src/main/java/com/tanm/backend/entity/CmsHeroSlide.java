package com.tanm.backend.entity;

import com.tanm.backend.common.BaseEntity;
import com.tanm.backend.enums.CmsStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cms_hero_slides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsHeroSlide extends BaseEntity {

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "subtitle", length = 255)
    private String subtitle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "primary_cta_text", length = 100)
    private String primaryCtaText;

    @Column(name = "primary_cta_url", length = 500)
    private String primaryCtaUrl;

    @Column(name = "secondary_cta_text", length = 100)
    private String secondaryCtaText;

    @Column(name = "secondary_cta_url", length = 500)
    private String secondaryCtaUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "background_image_id")
    private Media backgroundImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mobile_image_id")
    private Media mobileImage;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "overlay_opacity")
    @Builder.Default
    private Double overlayOpacity = 0.4;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CmsStatus status = CmsStatus.DRAFT;

    @Column(name = "publish_at")
    private LocalDateTime publishAt;

    @Column(name = "unpublish_at")
    private LocalDateTime unpublishAt;
}
