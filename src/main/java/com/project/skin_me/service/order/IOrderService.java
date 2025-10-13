package com.project.skin_me.service.order;

import com.project.skin_me.dto.OrderDto;
import com.project.skin_me.model.Order;

import java.util.List;

public interface IOrderService {

    Order placeOrderItem(Long userId);
    OrderDto getOrder(Long orderId);
    List<OrderDto> getUserOrders(Long userId);
    List<OrderDto> getAllUserOrders();
    OrderDto convertToDto(Order order);

}