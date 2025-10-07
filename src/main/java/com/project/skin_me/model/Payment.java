package com.project.skin_me.model;

import com.project.skin_me.enums.OrderStatus;
import com.project.skin_me.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payments")

public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private BigDecimal amount;

    // Payment method (e.g. BAKONG_QR, CREDIT_CARD)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    // Payment status (Pending / Success / Failed)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // Unique transaction reference from Bakong API
    @Column(name = "transaction_ref", unique = true)
    private String transactionRef;

    // Customer’s Bakong wallet ID (optional)
    @Column(name = "payer_account")
    private String payerAccount;

    // Merchant Bakong account ID
    @Column(name = "merchant_account")
    private String merchantAccount;

    // Time the transaction was made
    @Column(name = "transaction_time")
    private LocalDateTime transactionTime;

    // Message from Bakong API or internal error
    private String message;
}
