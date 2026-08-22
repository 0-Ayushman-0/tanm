package com.tanm.backend.entity;

import com.tanm.backend.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cms_seo_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeoMetadata extends BaseEntity {

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "keywords", length = 500)
    private String keywords;

    @Column(name = "canonical_url", length = 500)
    private String canonicalUrl;

    @Column(name = "og_title", length = 255)
    private String ogTitle;

    @Column(name = "og_description", length = 1000)
    private String ogDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "og_image_id")
    private Media ogImage;

    @Column(name = "robots", length = 100)
    @Builder.Default
    private String robots = "index, follow";
}
