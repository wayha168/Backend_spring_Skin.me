package com.project.skin_me.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PopularProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double sellRecord;
    private Integer quantitySold;
    private LocalDateTime lastPurchasedDate;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
