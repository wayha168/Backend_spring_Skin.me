package com.project.skin_me.dto;

import com.project.skin_me.model.FavoriteProduct.FavoriteProductBuilder;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteProductDto {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private String productBrand;
    public static FavoriteProductBuilder builder() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'builder'");
    }
}
