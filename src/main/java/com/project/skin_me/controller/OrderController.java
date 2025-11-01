package com.project.skin_me.controller;

import com.project.skin_me.dto.OrderDto;
import com.project.skin_me.exception.AlreadyExistsException;
import com.project.skin_me.exception.ResourceNotFoundException;
import com.project.skin_me.model.Order;
import com.project.skin_me.response.ApiResponse;
import com.project.skin_me.service.order.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/orders")
public class OrderController {

    private final IOrderService orderService;

    @PostMapping("/order")
    public ResponseEntity<ApiResponse> createOrder(@RequestParam Long userId) {
        try {
            Order order = orderService.placeOrderItem(userId);
            OrderDto orderDto = orderService.convertToDto(order);
            return ResponseEntity.ok(new ApiResponse("Order successfully placed", orderDto));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(CONFLICT)
                    .body(new ApiResponse(e.getMessage(), "Error Occurred!"));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable Long orderId) {
        try {
            OrderDto order = orderService.getOrder(orderId);
            return ResponseEntity.ok(new ApiResponse("Order fetched!", order));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse("Order not found", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getUserOrders(@PathVariable Long userId) {
        try {
            List<OrderDto> orders = orderService.getUserOrders(userId);
            return ResponseEntity.ok(new ApiResponse("User orders fetched!", orders));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse("User orders not found", e.getMessage()));
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> getAllOrders() {
        try {
            List<OrderDto> orders = orderService.getAllUserOrders();
            return ResponseEntity.ok(new ApiResponse("All orders fetched!", orders));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse("Error fetching orders", e.getMessage()));
        }
    }

    @PutMapping("/{orderId}/ship")
    public ResponseEntity<ApiResponse> markAsShipped(@PathVariable Long orderId,
                                                     @RequestParam(required = false) String trackingNumber) {
        Order order = orderService.markAsShipped(orderId, trackingNumber);
        return ResponseEntity.ok(new ApiResponse("Order marked as shipped", order));
    }

    @PutMapping("/{orderId}/deliver")
    public ResponseEntity<ApiResponse> markAsDelivered(@PathVariable Long orderId) {
        Order order = orderService.markAsDelivered(orderId);
        return ResponseEntity.ok(new ApiResponse("Order marked as delivered", order));
    }
}
