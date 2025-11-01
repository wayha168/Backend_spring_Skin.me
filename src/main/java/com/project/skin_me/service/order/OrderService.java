package com.project.skin_me.service.order;

import com.project.skin_me.dto.OrderDto;
import com.project.skin_me.enums.OrderStatus;
import com.project.skin_me.exception.ResourceNotFoundException;
import com.project.skin_me.model.*;
import com.project.skin_me.repository.OrderRepository;
import com.project.skin_me.repository.PaymentRepository;
import com.project.skin_me.repository.ProductRepository;
import com.project.skin_me.service.cart.ICartService;
import com.project.skin_me.service.popularProduct.IPopularProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;
    private final ICartService cartService;
    private final IPopularProductService popularProductService;

    @Override
    @Transactional
    public Order placeOrderItem(Long userId) {
        Cart cart = cartService.getCartByUserId(userId);
        Order order = createOrder(cart);

        List<OrderItem> orderItemList = createOrderItems(order, cart);
        order.setOrderItems(new HashSet<>(orderItemList));
        order.setOrderTotalAmount(calculateTotalAmount(orderItemList));
        Order savedOrder = orderRepository.save(order);
//        cartService.removeCart(cart.getId());
        return  savedOrder;
    }

    private Order createOrder(Cart cart) {

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product.getInventory() < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for " + product.getName());            }
        }

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDate.now());
        return order;
    }

    private List<OrderItem> createOrderItems(Order order, Cart cart) {
        // Do not deduct inventory here; move to payment confirmation
        return cart.getItems().stream().map(cartItem -> {
            return new OrderItem(
                    order,
                    cartItem.getProduct(),
                    cartItem.getQuantity(),
                    cartItem.getUnitPrice());
        }).toList();
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> orderItemList) {

        return orderItemList.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public OrderDto getOrder(Long orderId) {
        return orderRepository.findById(orderId).map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    public List<OrderDto> getUserOrders(Long userId){
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream().map(this::convertToDto).toList();
    }

    @Override
    public List<OrderDto> getAllUserOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(this::convertToDto).toList();
    }

    @Override
    public OrderDto convertToDto(Order order) {
        return modelMapper.map(order, OrderDto.class);
    }

//    public void updatePopularProducts(List<OrderItem> items) {
//        for (OrderItem item : items) {
//            Product product = item.getProduct();
//            product.setTotalOrders(product.getTotalOrders() + item.getQuantity());
//
//            // Example rule: if ordered more than 50 times -> mark as popular
//            if (product.getTotalOrders() >= 50 && product.getPopularProduct() == null) {
//                PopularProduct popular = new PopularProduct();
//                popular.setSellRecord(product.getTotalOrders());
//                popularProductRepository.save(popular);
//                product.setPopularProduct(popular);
//            }
//
//            productRepository.save(product);
//        }
//    }

    @Transactional
    public void updateOrder(Order order) {
        orderRepository.save(order);
    }

    @Override
    public Optional<Order> getOrderByStripeSessionId(String sessionId) {
        return orderRepository.findByStripeSessionId(sessionId);
    }

    @Override
    @Transactional
    public void confirmOrderPayment(Order order) {
        if (order.getOrderStatus() != OrderStatus.PENDING && order.getOrderStatus() != OrderStatus.PAYMENT_PENDING) {
            return;
        }

        // Deduct inventory
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            if (product.getInventory() < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for " + product.getName() + " during confirmation");
            }
            product.setInventory(product.getInventory() - item.getQuantity());
            productRepository.save(product);
        }
        // Update popular products
        popularProductService.saveFromOrder(order);
        // Remove cart
        Cart cart = cartService.getCartByUserId(order.getUser().getId());
        if (cart != null) {
            cartService.removeCart(cart.getId());
        }
        // Update payment record
        Payment payment = paymentRepository.findByTransactionRef(order.getStripeSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for sessionId: " + order.getStripeSessionId()));
        payment.setStatus(OrderStatus.SUCCESS);
        payment.setTransactionTime(LocalDateTime.now());
        paymentRepository.save(payment);

        // Update order status
        order.setOrderStatus(OrderStatus.PAID);
        updateOrder(order);
    }
    @Override
    @Transactional
    public Order markAsShipped(Long orderId, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (trackingNumber == null || trackingNumber.isBlank()) {
            trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        order.setTrackingNumber(trackingNumber);
        order.setOrderStatus(OrderStatus.SHIPPED);
        order.setShippedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order markAsDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        order.setOrderStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        return orderRepository.save(order);
    }
}
