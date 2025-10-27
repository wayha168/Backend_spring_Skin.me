package com.project.skin_me.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PopularProductDto {
    private Long productId;
    private String name;
    private BigDecimal price;
    private String brand;
    private Integer quantitySold;
    private LocalDateTime lastPurchasedDate;
}