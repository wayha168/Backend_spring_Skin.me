package com.project.skin_me.service.delivery;

import com.project.skin_me.model.Order;

public interface IDeliveryService {
    Order createShipment(Long orderId);
    Order markAsDelivered(Long orderId);
    Order trackOrder(Long orderId);
}
