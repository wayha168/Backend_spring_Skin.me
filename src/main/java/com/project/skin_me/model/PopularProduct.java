package com.project.skin_me.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @OneToMany(mappedBy = "popularProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;

}
