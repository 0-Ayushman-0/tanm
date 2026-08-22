package com.tanm.backend.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistDto {
    private Long id;
    private Long userId;
    private List<WishlistItemDto> items;
    private int totalItems;
}
