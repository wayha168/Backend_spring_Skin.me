package com.project.skin_me.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.skin_me.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    private LocalDate orderDate;
    private BigDecimal orderTotalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(name = "stripe_session_id")
    private String stripeSessionId;

    @Column(name = "transaction_ref")
    private String transactionRef;

    private String trackingNumber;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<OrderItem> orderItems = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;

    public Long getId() {
        return orderId;
    }

    public void setStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setShippedAt(LocalDateTime now) {
        this.shippedAt = now;
    }

    public void setDeliveredAt(LocalDateTime now) {
        this.deliveredAt = now;
    }
}
