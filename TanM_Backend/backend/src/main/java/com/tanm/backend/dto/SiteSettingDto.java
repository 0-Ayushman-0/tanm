package com.tanm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteSettingDto {
    private Long id;

    @NotBlank(message = "Setting key is required")
    private String key;
    private String value;
    private String settingGroup;
    private String valueType;
}
