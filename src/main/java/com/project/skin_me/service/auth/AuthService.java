package com.project.skin_me.service.auth;

import com.project.skin_me.model.Role;
import com.project.skin_me.model.User;
import com.project.skin_me.repository.RoleRespository;
import com.project.skin_me.repository.UserRepository;
import com.project.skin_me.request.LoginRequest;
import com.project.skin_me.request.SignupRequest;
import com.project.skin_me.response.ApiResponse;
import com.project.skin_me.response.JwtResponse;
import com.project.skin_me.security.jwt.JwtUtils;
import com.project.skin_me.security.user.ShopUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRespository roleRespository;

    public ResponseEntity<ApiResponse> login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(), loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateTokenForUser(authentication);
            ShopUserDetails userDetails = (ShopUserDetails) authentication.getPrincipal();

        java.util.Set<String> roles = userDetails.getAuthorities().stream()
            .map(a -> a.getAuthority())
            .collect(java.util.stream.Collectors.toSet());

        JwtResponse jwtResponse = new JwtResponse(userDetails.getId(), jwt, roles);
            return ResponseEntity.ok(new ApiResponse("Login success", jwtResponse));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(UNAUTHORIZED)
                    .body(new ApiResponse("Invalid email or password", null));
        }
    }

    public ResponseEntity<ApiResponse> signup(SignupRequest signupRequest) {
        if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse("Passwords do not match", null));
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.status(CONFLICT)
                    .body(new ApiResponse("Email already exists", null));
        }

        Role userRole = roleRespository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found."));

        User newUser = new User();
        newUser.setFirstName(signupRequest.getFirstName());
        newUser.setLastName(signupRequest.getLastName());
        newUser.setEmail(signupRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        newUser.setConfirmPassword(passwordEncoder.encode(signupRequest.getConfirmPassword()));

        newUser.setRoles(Collections.singleton(userRole));

        userRepository.save(newUser);

        return ResponseEntity.status(CREATED)
                .body(new ApiResponse("User registered successfully", null));
    }

    public ResponseEntity<ApiResponse> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new ApiResponse("Logout successful", null));
    }

    public ResponseEntity<ApiResponse> resetPassword(String email, String newPassword, String confirmPassword) {
        Optional<User> optionalUser = Optional.ofNullable(userRepository.findByEmail(email));

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse("Invalid email", null));
        }

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse("Passwords do not match", null));
        }

        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setConfirmPassword(passwordEncoder.encode(confirmPassword));
        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse("Password reset successful", null));
    }
}
