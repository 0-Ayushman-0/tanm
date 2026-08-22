package com.tanm.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReorderRequest {

    @NotEmpty(message = "Ordered list of IDs cannot be empty")
    private List<Long> orderedIds;
}
