package com.project.skin_me.repository;

import com.project.skin_me.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRespository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String role);

}

