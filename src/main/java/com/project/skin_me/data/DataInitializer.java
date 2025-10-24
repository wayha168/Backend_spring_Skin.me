package com.project.skin_me.data;

import com.project.skin_me.model.Role;
import com.project.skin_me.model.User;
import com.project.skin_me.repository.RoleRepository;
import com.project.skin_me.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("Initializing default roles and admin users");
        createDefaultRolesIfNotExists();
        createDefaultAdminsIfNotExists();
    }

    private void createDefaultRolesIfNotExists() {
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            roleRepository.save(userRole);
            logger.info("Created ROLE_USER");
        }
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepository.save(adminRole);
            logger.info("Created ROLE_ADMIN");
        }
    }

    private void createDefaultAdminsIfNotExists() {
        Optional<Role> adminRoleOpt = roleRepository.findByName("ROLE_ADMIN");
        if (adminRoleOpt.isEmpty()) {
            logger.error("ROLE_ADMIN not found! Skipping admin creation.");
            return;
        }
        Role adminRole = adminRoleOpt.get();

        // Create up to 3 default admin users
        for (int i = 1; i <= 3; i++) {
            String defaultEmail = "admin" + i + "@gmail.com";

            if (userRepository.existsByEmail(defaultEmail)) {
                logger.info("Admin user already exists: {}", defaultEmail);
                continue;
            }

            User admin = new User();
            admin.setEmail(defaultEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setConfirmPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEnabled(true);
            admin.setRegistrationDate(LocalDateTime.now());
            admin.setIsOnline(false);
            admin.setRoles(new HashSet<>(Collections.singletonList(adminRole)));

            userRepository.save(admin);
            logger.info("Created default admin user: {}", defaultEmail);
        }
    }
}
