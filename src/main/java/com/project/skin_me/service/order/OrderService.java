package com.project.skin_me.service.order;

import com.project.skin_me.dto.OrderDto;
import com.project.skin_me.enums.OrderStatus;
import com.project.skin_me.exception.ResourceNotFoundException;
import com.project.skin_me.model.*;
import com.project.skin_me.repository.OrderRepository;
import com.project.skin_me.repository.ProductRepository;
import com.project.skin_me.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final ModelMapper modelMapper;


    @Override
    @Transactional
    public Order placeOrderItem(Long userId) {
        Cart cart = cartService.getCartByUserId(userId);
        Order order = createOrder(cart);

        List<OrderItem> orderItemList = createOrderItems(order, cart);
        order.setOrderItems(new HashSet<>(orderItemList));
        order.setOrderTotalAmount(calculateTotalAmount(orderItemList));
        Order savedOrder = orderRepository.save(order);
        cartService.removeCart(cart.getId());

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
        //record order by deduct Inventory and also record
        return cart.getItems().stream().map(cartItem ->  {
                Product product = cartItem.getProduct();
                product.setInventory(product.getInventory() - cartItem.getQuantity());
                productRepository.save(product);
                return new OrderItem(
                        order,
                        product,
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
    public OrderDto convertToDto(Order order) {
        return modelMapper.map(order, OrderDto.class);
    }

}
