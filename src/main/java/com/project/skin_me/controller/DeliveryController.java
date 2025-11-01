package com.project.skin_me.controller;

import com.project.skin_me.model.Order;
import com.project.skin_me.response.ApiResponse;
import com.project.skin_me.service.delivery.IDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("${api.prefix}/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final IDeliveryService deliveryService;

    // 📦 Create shipment after payment success (admin or webhook action)
    @PostMapping("/ship/{orderId}")
    public ResponseEntity<ApiResponse> createShipment(@PathVariable Long orderId) {
        try {
            Order order = deliveryService.createShipment(orderId);
            return ResponseEntity.ok(new ApiResponse("Shipment created successfully", order));
        } catch (Exception e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse("Error creating shipment", e.getMessage()));
        }
    }

    // ✅ Mark order as delivered
    @PostMapping("/delivered/{orderId}")
    public ResponseEntity<ApiResponse> markAsDelivered(@PathVariable Long orderId) {
        try {
            Order order = deliveryService.markAsDelivered(orderId);
            return ResponseEntity.ok(new ApiResponse("Order marked as delivered", order));
        } catch (Exception e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse("Error marking order delivered", e.getMessage()));
        }
    }

    // 🔍 Track order
    @GetMapping("/track/{orderId}")
    public ResponseEntity<ApiResponse> trackOrder(@PathVariable Long orderId) {
        try {
            Order order = deliveryService.trackOrder(orderId);
            return ResponseEntity.ok(new ApiResponse("Tracking information retrieved", order));
        } catch (Exception e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse("Error tracking order", e.getMessage()));
        }
    }
}
