package com.project.skin_me.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.project.skin_me.model.Role;
import com.project.skin_me.model.User;
import com.project.skin_me.repository.RoleRepository;
import com.project.skin_me.repository.UserRepository;
import com.project.skin_me.request.LoginRequest;
import com.project.skin_me.request.SignupRequest;
import com.project.skin_me.response.ApiResponse;
import com.project.skin_me.response.JwtResponse;
import com.project.skin_me.security.jwt.JwtUtils;
import com.project.skin_me.security.user.ShopUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${app.oauth.redirect-uri:https://skinme.store/oauth-callback}")
    private String googleRedirectUri;

    @Override
    public ResponseEntity<ApiResponse> login(LoginRequest loginRequest, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateTokenForUser(authentication);
            ShopUserDetails userDetails = (ShopUserDetails) authentication.getPrincipal();

            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(Collectors.toSet());

            Cookie cookie = new Cookie("token", jwt);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            cookie.setAttribute("SameSite", "Strict");
            response.addCookie(cookie);

            JwtResponse jwtResponse = new JwtResponse(userDetails.getId(), jwt, roles);
            logger.info("User logged in: {}", userDetails.getEmail());
            return ResponseEntity.ok(new ApiResponse("Login success", jwtResponse));

        } catch (AuthenticationException e) {
            logger.warn("Login failed for email: {}. Error: {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(UNAUTHORIZED)
                    .body(new ApiResponse("Invalid email or password", null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse> googleLogin(String code, HttpServletResponse response) {
        try {
            logger.debug("Processing Google OAuth2 login with code: {}", code);
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    "https://oauth2.googleapis.com/token",
                    googleClientId,
                    googleClientSecret,
                    code,
                    googleRedirectUri).execute();

            GoogleIdToken idToken = tokenResponse.parseIdToken();
            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String googleId = payload.getSubject();
            String firstName = payload.get("given_name") != null ? payload.get("given_name").toString() : "";
            String lastName = payload.get("family_name") != null ? payload.get("family_name").toString() : "";
            logger.debug("Google user info: email={}, googleId={}", email, googleId);

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                Role userRole = roleRepository.findByName("ROLE_USER")
                        .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found."));
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setGoogleId(googleId);
                newUser.setFirstName(firstName);
                newUser.setLastName(lastName);
                newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                newUser.setConfirmPassword(newUser.getPassword());
                newUser.setEnabled(true);
                newUser.setRoles(new HashSet<>(Collections.singletonList(userRole)));
                logger.info("Creating new user for Google login: {}", email);
                return userRepository.save(newUser);
            });

            ShopUserDetails userDetails = ShopUserDetails.buildUserDetails(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            String jwt = jwtUtils.generateTokenForUser(authentication);

            Cookie cookie = new Cookie("token", jwt);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            cookie.setAttribute("SameSite", "Strict");
            response.addCookie(cookie);

            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(Collectors.toSet());

            JwtResponse jwtResponse = new JwtResponse(user.getId(), jwt, roles);
            logger.info("Google login successful for user: {}", email);
            return ResponseEntity.ok(new ApiResponse("Google login success", jwtResponse));

        } catch (Exception e) {
            logger.error("Google login failed: {}", e.getMessage(), e);
            return ResponseEntity.status(UNAUTHORIZED)
                    .body(new ApiResponse("Google login failed: " + e.getMessage(), null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse> signup(SignupRequest signupRequest) {
        try {
            if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
                logger.warn("Signup failed: Passwords do not match for email: {}", signupRequest.getEmail());
                return ResponseEntity.status(BAD_REQUEST)
                        .body(new ApiResponse("Passwords do not match", null));
            }

            if (userRepository.existsByEmail(signupRequest.getEmail())) {
                logger.warn("Signup failed: Email already exists: {}", signupRequest.getEmail());
                return ResponseEntity.status(CONFLICT)
                        .body(new ApiResponse("Email already exists", null));
            }

            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> {
                        logger.error("Default role ROLE_USER not found");
                        return new RuntimeException("Default role ROLE_USER not found.");
                    });

            User newUser = new User();
            newUser.setFirstName(signupRequest.getFirstName());
            newUser.setLastName(signupRequest.getLastName());
            newUser.setEmail(signupRequest.getEmail());
            newUser.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
            newUser.setConfirmPassword(passwordEncoder.encode(signupRequest.getConfirmPassword()));
            newUser.setEnabled(true);
            newUser.setRoles(new HashSet<>(Collections.singletonList(userRole)));

            userRepository.save(newUser);
            logger.info("User registered successfully: {}", signupRequest.getEmail());
            return ResponseEntity.status(CREATED)
                    .body(new ApiResponse("User registered successfully", null));
        } catch (Exception e) {
            logger.error("Signup failed for email: {}. Error: {}", signupRequest.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Signup failed: " + e.getMessage(), null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse> logout() {
        SecurityContextHolder.clearContext();
        logger.info("User logged out");
        return ResponseEntity.ok(new ApiResponse("Logout successful", null));
    }

    @Override
    public ResponseEntity<ApiResponse> resetPassword(String email, String newPassword, String confirmPassword) {
        try {
            Optional<User> optionalUser = userRepository.findByEmail(email);

            if (optionalUser.isEmpty()) {
                logger.warn("Password reset failed: Invalid email: {}", email);
                return ResponseEntity.status(NOT_FOUND)
                        .body(new ApiResponse("Invalid email", null));
            }

            if (!newPassword.equals(confirmPassword)) {
                logger.warn("Password reset failed: Passwords do not match for email: {}", email);
                return ResponseEntity.status(BAD_REQUEST)
                        .body(new ApiResponse("Passwords do not match", null));
            }

            User user = optionalUser.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setConfirmPassword(passwordEncoder.encode(confirmPassword));
            userRepository.save(user);
            logger.info("Password reset successful for email: {}", email);
            return ResponseEntity.ok(new ApiResponse("Password reset successful", null));
        } catch (Exception e) {
            logger.error("Password reset failed for email: {}. Error: {}", email, e.getMessage(), e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Password reset failed: " + e.getMessage(), null));
        }
    }
}