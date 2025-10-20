package com.project.skin_me.service.favorite;

import com.project.skin_me.dto.FavoriteProductDto;

import java.util.List;

public interface IFavoriteService {

    FavoriteProductDto addFavorite(Long userId, Long productId);

    List<FavoriteProductDto> getFavoritesByUser(Long userId);

    void removeFavorite(Long userId, Long productId);

    FavoriteProductDto convertToDto(Object favoriteItem);

    List<FavoriteProductDto> getAllFavorites();
}