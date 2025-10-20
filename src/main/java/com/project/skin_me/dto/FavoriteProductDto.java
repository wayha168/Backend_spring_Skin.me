package com.project.skin_me.dto;

import com.project.skin_me.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteProductDto {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private String productBrand;
    private Product product;
}
