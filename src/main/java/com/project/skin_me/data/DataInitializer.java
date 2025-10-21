package com.project.skin_me.data;

import com.project.skin_me.model.Role;
import com.project.skin_me.model.User;
import com.project.skin_me.repository.RoleRepository;
import com.project.skin_me.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Transactional
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final UserRepository userRepository;
    private final RoleRepository roleRespository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Set<String> defaultRoles = Set.of("ROLE_ADMIN", "ROLE_USER");

        createDefaultUsersIfNotExits();
        createDefaultRoleIfNotExits(defaultRoles);
        createDefaultAdminsIfNotExits();
    }

    private void createDefaultRoleIfNotExits(Set<String> roles){
        roles.stream()
                .filter(role -> roleRespository.findByName(role).isEmpty())
                .map(Role:: new)
                .forEach(roleRespository::save);

        System.out.println("✅ Default roles ensured: " + roles);
    }

    private void createDefaultUsersIfNotExits() {
        Optional<Role> userRoleOpt = roleRespository.findByName("ROLE_USER");
        if (userRoleOpt.isEmpty()) {
            System.err.println("⚠️ ROLE_USER not found! Skipping user creation.");
            return;
        }
        Role userRole = userRoleOpt.get();

        for (int i = 1; i <= 5 ; i++) {
            String defaultEmail = "user" + i + "@gmail.com";
            if (userRepository.existsByEmail(defaultEmail)) {
                continue;
            }
                User user = new User();
                user.setFirstName("User");
                user.setLastName("Number" + i);
                user.setEmail(defaultEmail);
                user.setPassword(passwordEncoder.encode("user" + i));
                user.setConfirmPassword(passwordEncoder.encode("user" +i));
                user.setRoles(Set.of(userRole));
                userRepository.save(user);

                System.out.println("Default user created" + i + "successfully.");
        }
    }

    private void createDefaultAdminsIfNotExits(){
        Optional<Role> adminRoleOpt = roleRespository.findByName("ROLE_ADMIN");
        if (adminRoleOpt.isEmpty()) {
            System.err.println("ROLE_ADMIN not found! Skipping admin creation.");
            return;
        }
        Role adminRole = adminRoleOpt.get();

        for (int i = 1; i <= 3; i++) {
            String defaultEmail = "admin" + i + "@email.com";
            if (userRepository.existsByEmail(defaultEmail)) {
                continue;
            }
            User user = new User();
            user.setFirstName("Admin");
            user.setLastName("Admin" + i);
            user.setEmail(defaultEmail);
            user.setPassword(passwordEncoder.encode("admin" + i));
            user.setConfirmPassword(passwordEncoder.encode("admin" +i));
            user.setRoles(Set.of(adminRole));
            userRepository.save(user);
            System.out.println("Default admin user " + i + " created successfully.");
        }
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
