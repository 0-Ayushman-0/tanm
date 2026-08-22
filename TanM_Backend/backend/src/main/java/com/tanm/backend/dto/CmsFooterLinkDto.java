package com.tanm.backend.dto;

import com.tanm.backend.enums.CmsStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsFooterLinkDto {
    private Long id;
    private Long sectionId;

    @NotBlank(message = "Link label is required")
    private String label;

    @NotBlank(message = "URL is required")
    private String url;

    private int displayOrder;
    private boolean isExternal;
    private CmsStatus status;
}
