package com.project.skin_me.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private double discount;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonBackReference
    @JsonIgnoreProperties({"items"})
    @JoinColumn(name = "cart_id")
    @JsonIgnore
    private Cart cart;

    public void setTotalPrice() {
        this.totalPrice = this.unitPrice.multiply(new BigDecimal(quantity));
    }


}
