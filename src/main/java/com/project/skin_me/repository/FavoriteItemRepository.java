package com.project.skin_me.repository;

import com.project.skin_me.model.FavoriteItem;
import com.project.skin_me.model.Product;
import com.project.skin_me.model.FavoriteList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteItemRepository extends JpaRepository<FavoriteItem, Long> {
    Optional<FavoriteItem> findByFavoriteListAndProduct(FavoriteList favoriteList, Product product);
}
