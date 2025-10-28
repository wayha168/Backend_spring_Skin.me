package com.project.skin_me.service.cart;

import com.project.skin_me.model.Cart;

public interface ICartItemService {
    void addItemToCart(Cart cart, Long productId, int quantity);
    void removeItemFromCart(Long cartId, Long productId);
    void updateItemQuantity(Long cartId, Long productId, int quantity);
}
