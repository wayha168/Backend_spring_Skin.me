package com.project.skin_me.repository;

import com.project.skin_me.model.FavoriteProduct;
import com.project.skin_me.model.User;
import com.project.skin_me.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteProductRepository extends JpaRepository<FavoriteProduct, Long> {

    List<FavoriteProduct> findByUser(User user);

    Optional<FavoriteProduct> findByUserAndProduct(User user, Product product);

    void deleteByUserAndProduct(User user, Product product);
}
