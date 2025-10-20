package com.project.skin_me.repository;

import com.project.skin_me.model.FavoriteList;
import com.project.skin_me.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteListRepository extends JpaRepository<FavoriteList, Long> {
    Optional<FavoriteList> findByUser(User user);
}
