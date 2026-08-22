package com.tanm.backend.dto;

import com.tanm.backend.enums.CmsStatus;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkActionRequest {

    @NotEmpty(message = "Item IDs list cannot be empty")
    private List<Long> ids;

    private String action; // e.g. "STATUS_CHANGE", "DELETE"
    private CmsStatus status;
}
