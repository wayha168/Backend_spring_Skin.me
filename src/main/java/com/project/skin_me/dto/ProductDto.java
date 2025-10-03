package com.project.skin_me.dto;

import java.math.BigDecimal;
import java.util.List;

import com.project.skin_me.model.Category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {

    private Long id;
    private String name;
    private String brand;
    private BigDecimal price;
    private String productType;
    private int inventory;
    private String description;
    private Category category;
    private List<ImageDto> images; 

}
