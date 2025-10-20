package com.project.skin_me.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteProductDto {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private String productBrand;
}
