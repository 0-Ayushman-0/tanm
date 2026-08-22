package com.tanm.backend.entity;

import com.tanm.backend.common.BaseEntity;
import com.tanm.backend.enums.CmsStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cms_announcement_bars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsAnnouncementBar extends BaseEntity {

    @Column(name = "text", nullable = false, length = 500)
    private String text;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "background_color", length = 30)
    @Builder.Default
    private String backgroundColor = "#000000";

    @Column(name = "text_color", length = 30)
    @Builder.Default
    private String textColor = "#FFFFFF";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CmsStatus status = CmsStatus.DRAFT;
}
