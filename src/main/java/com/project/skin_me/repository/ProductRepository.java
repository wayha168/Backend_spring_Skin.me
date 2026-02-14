package com.project.skin_me.repository;

import java.util.List;

import com.project.skin_me.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.skin_me.model.Product;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory_Name(String categoryName);

    List<Product> findByBrand(String brand);

    List<Product> findByName(String name);

    List<Product> findByStatus(ProductStatus status);

    List<Product> findByProductType(String productType);

    List<Product> findByCategory_NameAndBrand(String categoryName, String brand);

    List<Product> findByBrandAndName(String brand, String name);

    List<Product> findByProductTypeAndName(String productType, String name);

    Long countByBrandAndName(String brand, String name);

    boolean existsByNameAndBrand(String name, String brand);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.category")
    List<Product> findAllWithImages();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category")
    List<Product> findAllWithCategory();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.category.name = :categoryName")
    List<Product> findByCategoryNameWithCategory(String categoryName);
}
