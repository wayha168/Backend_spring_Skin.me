package com.project.skin_me.repository;

import com.project.skin_me.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByUserId(Long userId);
    Optional<Order> findByTransactionRef(String transactionRef);
    Optional<Order> findByStripeSessionId(String sessionId);
}
