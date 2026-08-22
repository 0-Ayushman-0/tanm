package com.tanm.backend.entity;

import com.tanm.backend.common.BaseEntity;
import com.tanm.backend.enums.CmsStatus;
import com.tanm.backend.enums.MenuType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cms_navigation_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsNavigationItem extends BaseEntity {

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @Column(name = "url", length = 500)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CmsNavigationItem parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CmsNavigationItem> children = new ArrayList<>();

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "menu_type", nullable = false, length = 30)
    @Builder.Default
    private MenuType menuType = MenuType.CUSTOM;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "is_external", nullable = false)
    @Builder.Default
    private boolean isExternal = false;

    @Column(name = "icon", length = 100)
    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CmsStatus status = CmsStatus.PUBLISHED;
}
