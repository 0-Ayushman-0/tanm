package com.tanm.backend.entity;

import com.tanm.backend.common.BaseEntity;
import com.tanm.backend.enums.BannerType;
import com.tanm.backend.enums.CmsStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cms_banners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsBanner extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "banner_type", nullable = false, length = 50)
    private BannerType bannerType;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "subtitle", length = 255)
    private String subtitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desktop_image_id")
    private Media desktopImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mobile_image_id")
    private Media mobileImage;

    @Column(name = "button_text", length = 100)
    private String buttonText;

    @Column(name = "button_url", length = 500)
    private String buttonUrl;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "priority")
    @Builder.Default
    private int priority = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CmsStatus status = CmsStatus.DRAFT;
}
