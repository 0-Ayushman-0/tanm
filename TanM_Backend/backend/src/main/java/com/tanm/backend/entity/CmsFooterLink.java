package com.tanm.backend.entity;

import com.tanm.backend.common.BaseEntity;
import com.tanm.backend.enums.CmsStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cms_footer_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsFooterLink extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private CmsFooterSection section;

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    @Column(name = "is_external", nullable = false)
    @Builder.Default
    private boolean isExternal = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CmsStatus status = CmsStatus.PUBLISHED;
}
