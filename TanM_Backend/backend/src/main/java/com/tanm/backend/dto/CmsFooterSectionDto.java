package com.tanm.backend.dto;

import com.tanm.backend.enums.CmsStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsFooterSectionDto {
    private Long id;

    @NotBlank(message = "Section title is required")
    private String title;

    private int displayOrder;
    private CmsStatus status;
    private List<CmsFooterLinkDto> links;
}
