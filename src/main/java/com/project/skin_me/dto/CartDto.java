package com.project.skin_me.dto;

import com.project.skin_me.model.Cart;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class CartDto {

    private Long cartId;
    private Set<CartItemDto> items;
    private BigDecimal totalAmount;

    public CartDto(Cart cart) {
        this.cartId = cart.getId();
        this.totalAmount = cart.getTotalAmount();
        this.items = cart.getItems().stream()
                .map(CartItemDto::new)
                .collect(Collectors.toSet());
    }
}
