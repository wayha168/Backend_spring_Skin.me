package com.project.skin_me.service.favorite;

import com.project.skin_me.dto.FavoriteProductDto;
import com.project.skin_me.model.FavoriteProduct;

import java.util.List;

public interface IFavoriteProductService {

    FavoriteProductDto addFavorite(Long userId, Long productId);

    List<FavoriteProductDto> getFavoritesByUser(Long userId);

    void removeFavorite(Long userId, Long productId);

    FavoriteProductDto convertToDto(FavoriteProduct favoriteProduct);
    List<FavoriteProductDto> getAllFavorites();

}
