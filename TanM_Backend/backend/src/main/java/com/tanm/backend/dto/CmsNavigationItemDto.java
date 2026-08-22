package com.tanm.backend.dto;

import com.tanm.backend.enums.CmsStatus;
import com.tanm.backend.enums.MenuType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsNavigationItemDto {
    private Long id;

    @NotBlank(message = "Navigation label is required")
    private String label;

    private String url;
    private Long parentId;
    private List<CmsNavigationItemDto> children;
    private int displayOrder;
    private MenuType menuType;
    private Long targetId;
    private boolean isExternal;
    private String icon;
    private CmsStatus status;
}
