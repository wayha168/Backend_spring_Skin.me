package com.project.skin_me.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "favorite_list_id")
    @JsonIgnore
    private FavoriteList favoriteList;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
