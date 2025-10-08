package com.project.skin_me.service.popularProduct;

import com.project.skin_me.model.Order;
import com.project.skin_me.model.OrderItem;
import com.project.skin_me.model.PopularProduct;
import com.project.skin_me.repository.PopularProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PopularProductService implements IPopularProductService {

    private final PopularProductRepository popularProductRepository;

    @Override
    public void saveFromOrder(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            PopularProduct popular = new PopularProduct();
            popular.setProduct(item.getProduct());
            popular.setQuantitySold(item.getQuantity());
            popular.setLastPurchasedDate(java.time.LocalDateTime.now());
            popularProductRepository.save(popular);
        }
    }
}
