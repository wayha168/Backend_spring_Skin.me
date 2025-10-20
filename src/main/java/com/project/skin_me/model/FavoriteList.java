package com.project.skin_me.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "favoriteList", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<FavoriteItem> items = new HashSet<>();

    public void addItem(FavoriteItem item) {
        items.add(item);
        item.setFavoriteList(this);
    }

    public void removeItem(FavoriteItem item) {
        items.remove(item);
        item.setFavoriteList(null);
    }
}
