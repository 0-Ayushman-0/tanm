package com.tanm.backend.entity;

import com.tanm.backend.common.BaseEntity;
import com.tanm.backend.enums.CmsStatus;
import com.tanm.backend.enums.SectionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cms_section_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsSectionConfig extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false, unique = true, length = 50)
    private SectionType sectionType;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "subtitle", length = 255)
    private String subtitle;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CmsStatus status = CmsStatus.PUBLISHED;

    @Column(name = "configuration_json", columnDefinition = "TEXT")
    private String configurationJson;
}
