package com.project.skin_me.repository;

import com.project.skin_me.model.PopularProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface PopularProductRepository extends JpaRepository<PopularProduct,Long> {

    Optional<PopularProduct> findByProductId(Long id);
}
