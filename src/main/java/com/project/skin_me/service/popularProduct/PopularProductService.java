package com.project.skin_me.service.popularProduct;

import com.project.skin_me.dto.PopularProductDto;
import com.project.skin_me.model.Order;
import com.project.skin_me.model.OrderItem;
import com.project.skin_me.model.PopularProduct;
import com.project.skin_me.model.Product;
import com.project.skin_me.repository.PopularProductRepository;
import com.project.skin_me.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class PopularProductService implements IPopularProductService {

    private final PopularProductRepository popularProductRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private static final int POPULARITY_THRESHOLD = 10;

    @Override
    public void saveFromOrder(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            int added = item.getQuantity();
            product.setTotalOrders(product.getTotalOrders() + added);
            productRepository.save(product);

            PopularProduct popular = popularProductRepository.findByProductId(product.getId()).orElse(null);

            if (popular == null) {
                if (product.getTotalOrders() >= POPULARITY_THRESHOLD) {
                    popular = new PopularProduct();
                    popular.setProduct(product);
                    popular.setQuantitySold(product.getTotalOrders());
                    popular.setLastPurchasedDate(LocalDateTime.now());
                    popularProductRepository.save(popular);
                    product.setPopularProduct(popular);
                    productRepository.save(product);
                }
            } else {
                popular.setQuantitySold(product.getTotalOrders());
                popular.setLastPurchasedDate(LocalDateTime.now());
                popularProductRepository.save(popular);
            }
        }
    }

    @Override
    public Optional<PopularProduct> findByProductId(Long productId) {
        return popularProductRepository.findByProductId(productId);
    }

    public List<PopularProductDto> getPopularProducts() {
        return popularProductRepository.findAll()
                .stream()
                .filter(pop -> pop.getQuantitySold() >= POPULARITY_THRESHOLD)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public PopularProductDto convertToDto(PopularProduct popularProduct) {
        return modelMapper.map(popularProduct, PopularProductDto.class); // Assumes ModelMapper is configured
    }
}