package com.project.skin_me.dto;

import com.project.skin_me.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteProductDto {
    private Long id;
    private Long userId;
    private Long productId;
    private Product product;       // full product info
    private String productName;    // optional, for table display
    private String productBrand;   // optional
    private String productThumbnailUrl; // first image
}
