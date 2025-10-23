package com.project.skin_me.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.project.skin_me.enums.ActivityType;
import com.project.skin_me.model.Activity;
import com.project.skin_me.model.Role;
import com.project.skin_me.model.User;
import com.project.skin_me.repository.ActivityRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final ActivityRepository activityRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${app.oauth.redirect-uri:https://skinme.store/oauth-callback}")
    private String googleRedirectUri;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse> login(LoginRequest loginRequest, HttpServletResponse response) {
        try {
            logger.debug("Attempting login for email: {}", loginRequest.getEmail());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateTokenForUser(authentication);
            ShopUserDetails userDetails = (ShopUserDetails) authentication.getPrincipal();

            recordLogin(userDetails.getId());

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
        } catch (Exception e) {
            logger.error("Unexpected error during login for email: {}. Error: {}", loginRequest.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Login failed: " + e.getMessage(), null));
        }
    }

    @Override
    @Transactional
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
                        .orElseThrow(() -> {
                            logger.error("Default role ROLE_USER not found");
                            return new RuntimeException("Default role ROLE_USER not found.");
                        });
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setGoogleId(googleId);
                newUser.setFirstName(firstName);
                newUser.setLastName(lastName);
                newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                newUser.setEnabled(true);
                newUser.setRegistrationDate(LocalDateTime.now());
                newUser.setIsOnline(false);
                newUser.setRoles(new HashSet<>(Collections.singletonList(userRole)));
                logger.info("Creating new user for Google login: {}", email);
                return registerUser(newUser);
            });

            ShopUserDetails userDetails = ShopUserDetails.buildUserDetails(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            String jwt = jwtUtils.generateTokenForUser(authentication);

            recordLogin(user.getId());

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
    @Transactional
    public ResponseEntity<ApiResponse> signup(SignupRequest signupRequest) {
        try {
            // Validate that password and confirmPassword are provided and match
            if (signupRequest.getPassword() == null || signupRequest.getPassword().isBlank()) {
                logger.warn("Signup failed: Password is required for email: {}", signupRequest.getEmail());
                return ResponseEntity.status(BAD_REQUEST)
                        .body(new ApiResponse("Password is required", null));
            }
            if (signupRequest.getConfirmPassword() == null || signupRequest.getConfirmPassword().isBlank()) {
                logger.warn("Signup failed: Confirm password is required for email: {}", signupRequest.getEmail());
                return ResponseEntity.status(BAD_REQUEST)
                        .body(new ApiResponse("Confirm password is required", null));
            }
            if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
                logger.warn("Signup failed: Passwords do not match for email: {}", signupRequest.getEmail());
                return ResponseEntity.status(BAD_REQUEST)
                        .body(new ApiResponse("Passwords do not match", null));
            }

            // Check if email already exists
            if (userRepository.existsByEmail(signupRequest.getEmail())) {
                logger.warn("Signup failed: Email already exists: {}", signupRequest.getEmail());
                return ResponseEntity.status(CONFLICT)
                        .body(new ApiResponse("Email already exists", null));
            }

            // Retrieve default user role
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> {
                        logger.error("Default role ROLE_USER not found");
                        return new RuntimeException("Default role ROLE_USER not found.");
                    });

            // Create new user
            User newUser = new User();
            newUser.setFirstName(signupRequest.getFirstName());
            newUser.setLastName(signupRequest.getLastName());
            newUser.setEmail(signupRequest.getEmail());
            newUser.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
            newUser.setConfirmPassword(passwordEncoder.encode(signupRequest.getConfirmPassword()));
            newUser.setEnabled(true);
            newUser.setRegistrationDate(LocalDateTime.now());
            newUser.setIsOnline(false);
            newUser.setRoles(new HashSet<>(Collections.singletonList(userRole)));

            User savedUser = registerUser(newUser);
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
    @Transactional
    public ResponseEntity<ApiResponse> logout() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof ShopUserDetails) {
                ShopUserDetails userDetails = (ShopUserDetails) authentication.getPrincipal();
                recordLogout(userDetails.getId());
                SecurityContextHolder.clearContext();
                logger.info("User logged out: {}", userDetails.getEmail());
            } else {
                logger.info("User logged out (no user details available)");
            }
            return ResponseEntity.ok(new ApiResponse("Logout successful", null));
        } catch (Exception e) {
            logger.error("Logout failed: {}", e.getMessage(), e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Logout failed: " + e.getMessage(), null));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse> resetPassword(String email, String newPassword, String confirmPassword) {
        try {
            if (newPassword == null || newPassword.isBlank()) {
                logger.warn("Password reset failed: New password is required for email: {}", email);
                return ResponseEntity.status(BAD_REQUEST)
                        .body(new ApiResponse("New password is required", null));
            }
            if (confirmPassword == null || confirmPassword.isBlank()) {
                logger.warn("Password reset failed: Confirm password is required for email: {}", email);
                return ResponseEntity.status(BAD_REQUEST)
                        .body(new ApiResponse("Confirm password is required", null));
            }
            if (!newPassword.equals(confirmPassword)) {
                logger.warn("Password reset failed: Passwords do not match for email: {}", email);
                return ResponseEntity.status(BAD_REQUEST)
                        .body(new ApiResponse("Passwords do not match", null));
            }

            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isEmpty()) {
                logger.warn("Password reset failed: Invalid email: {}", email);
                return ResponseEntity.status(NOT_FOUND)
                        .body(new ApiResponse("Invalid email", null));
            }

            User user = optionalUser.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            recordPasswordReset(user.getId(), email);
            logger.info("Password reset successful for email: {}", email);
            return ResponseEntity.ok(new ApiResponse("Password reset successful", null));
        } catch (Exception e) {
            logger.error("Password reset failed for email: {}. Error: {}", email, e.getMessage(), e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Password reset failed: " + e.getMessage(), null));
        }
    }

    @Transactional
    public User registerUser(User user) {
        logger.debug("Registering user with email: {}", user.getEmail());
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("Email already exists: {}", user.getEmail());
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getRegistrationDate() == null) {
            user.setRegistrationDate(LocalDateTime.now());
        }
        user.setIsOnline(false);
        logger.debug("Setting isOnline to false for user: {}", user.getEmail());
        User savedUser = userRepository.save(user);

        Activity activity = new Activity();
        activity.setUser(savedUser);
        activity.setActivityType(ActivityType.REGISTER);
        activity.setTimestamp(LocalDateTime.now());
        activity.setDetails("User registered with email: " + user.getEmail());
        activityRepository.save(activity);

        logger.info("User registered successfully: {}", user.getEmail());
        return savedUser;
    }

    @Transactional
    public void recordLogin(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setLastLogin(LocalDateTime.now());
            logger.debug("Before setting isOnline to true for user ID: {}, current isOnline: {}", userId, user.isOnline());
            user.setIsOnline(true);
            logger.debug("After setting isOnline to true for user ID: {}", userId);
            userRepository.save(user);
            logger.debug("User saved with isOnline: {} for user ID: {}", user.isOnline(), userId);

            Activity activity = new Activity();
            activity.setUser(user);
            activity.setActivityType(ActivityType.LOGIN);
            activity.setTimestamp(LocalDateTime.now());
            activity.setDetails("User logged in");
            activityRepository.save(activity);

            logger.info("Login recorded for user ID: {}", userId);
        } else {
            logger.error("User not found with ID: {}", userId);
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }

    @Transactional
    public void recordLogout(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            logger.debug("Before setting isOnline to false for user ID: {}, current isOnline: {}", userId, user.isOnline());
            user.setIsOnline(false);
            logger.debug("After setting isOnline to false for user ID: {}", userId);
            userRepository.save(user);
            logger.debug("User saved with isOnline: {} for user ID: {}", user.isOnline(), userId);

            Activity activity = new Activity();
            activity.setUser(user);
            activity.setActivityType(ActivityType.LOGOUT);
            activity.setTimestamp(LocalDateTime.now());
            activity.setDetails("User logged out");
            activityRepository.save(activity);

            logger.info("Logout recorded for user ID: {}", userId);
        } else {
            logger.error("User not found with ID: {}", userId);
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }

    @Transactional
    public void recordPasswordReset(Long userId, String email) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Activity activity = new Activity();
            activity.setUser(user);
            activity.setActivityType(ActivityType.PASSWORD_RESET);
            activity.setTimestamp(LocalDateTime.now());
            activity.setDetails("Password reset for email: " + email);
            activityRepository.save(activity);

            logger.info("Password reset recorded for user ID: {}", userId);
        } else {
            logger.error("User not found with ID: {}", userId);
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }
}