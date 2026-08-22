package com.tanm.backend.dto;

import com.tanm.backend.enums.CmsStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsAnnouncementBarDto {
    private Long id;

    @NotBlank(message = "Announcement text is required")
    private String text;
    private String linkUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String backgroundColor;
    private String textColor;
    private CmsStatus status;
}
