package com.project.skin_me.service.popularProduct;

import com.project.skin_me.dto.PopularProductDto;
import com.project.skin_me.model.Order;
import com.project.skin_me.model.PopularProduct;

import java.util.List;
import java.util.Optional;

public interface IPopularProductService {
    void saveFromOrder(Order order);
    Optional<PopularProduct> findByProductId(Long productId);
    List<PopularProductDto> getPopularProducts();
}