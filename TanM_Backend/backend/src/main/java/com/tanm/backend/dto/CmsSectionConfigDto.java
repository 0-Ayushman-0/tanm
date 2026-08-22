package com.tanm.backend.dto;

import com.tanm.backend.enums.CmsStatus;
import com.tanm.backend.enums.SectionType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsSectionConfigDto {
    private Long id;

    @NotNull(message = "Section type is required")
    private SectionType sectionType;
    private String title;
    private String subtitle;
    private int displayOrder;
    private CmsStatus status;
    private String configurationJson;
}
