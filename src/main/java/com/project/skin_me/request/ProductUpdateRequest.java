package com.project.skin_me.request;

import java.math.BigDecimal;

import com.project.skin_me.model.Category;

import lombok.Data;

@Data
public class ProductUpdateRequest {
    private Long id;
    private String name;
    private String brand;
    private BigDecimal price;
    private String productType;
    private int inventory;
    private String description;
    private String howToUse;
    private Category category;
}
