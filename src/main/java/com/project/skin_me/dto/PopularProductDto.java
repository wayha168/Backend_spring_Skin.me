package com.project.skin_me.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PopularProductDto {
    private Long productId;
    private String productName;
    private String productBrand;
    private Integer quantitySold;
    private LocalDateTime lastPurchasedDate;
}