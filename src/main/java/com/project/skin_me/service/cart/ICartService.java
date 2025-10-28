package com.project.skin_me.service.cart;

import com.project.skin_me.model.Cart;
import com.project.skin_me.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ICartService {

    Cart getCart(Long id);

    void removeCart(Long id);

    BigDecimal getTotalPrice(Long id);

    Cart initializeNewCart(User user);

    Cart getCartByUserId(Long userId);

    List<Cart> getAllCarts();

    Optional<Cart> getUserActiveCart(User user);
}