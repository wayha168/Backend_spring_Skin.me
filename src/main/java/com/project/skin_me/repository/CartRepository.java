package com.project.skin_me.repository;

import com.project.skin_me.model.Cart;
import com.project.skin_me.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CartRepository extends JpaRepository<Cart,Long> {

    Optional<Cart> findByUserIdAndActive(Long userId, boolean active);

    Cart findByUserId(Long userId);
}
