package com.project.skin_me.service.delivery;

import com.project.skin_me.enums.OrderStatus;
import com.project.skin_me.exception.ResourceNotFoundException;
import com.project.skin_me.model.Order;
import com.project.skin_me.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService implements IDeliveryService {

    private final OrderRepository orderRepository;

    @Override
    public Order createShipment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setOrderStatus(OrderStatus.SHIPPED);
        order.setTrackingNumber("TRK-" + UUID.randomUUID());
        order.setShippedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    public Order markAsDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setOrderStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    public Order trackOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }
}
