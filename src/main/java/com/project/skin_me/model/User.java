package com.project.skin_me.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String firstName;
        private String lastName;

        @NaturalId
        @Column(unique = true, nullable = false)
        private String email;
        private String password;
        private String confirmPassword;

        @Column(name = "google_id")
        private String googleId;

        private boolean enabled = true;

        @Column(name = "registration_date", nullable = false)
        private LocalDateTime registrationDate;

        @Column(name = "last_login")
        private LocalDateTime lastLogin;

        @Column(name = "is_online")
        @JsonProperty("isOnline")
        private boolean isOnline = false;

        @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
        private Cart cart;

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Order> orders;

        @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
                        CascadeType.REFRESH })
        @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
        private Collection<Role> roles = new HashSet<>();

        @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
        @JoinColumn(name = "favorite_list_id")
        private FavoriteList favoriteList;

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Activity> activities;


        public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

        public boolean isOnline() {
        return isOnline;
    }
}
