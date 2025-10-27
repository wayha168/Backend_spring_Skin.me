package com.project.skin_me.service.user;

import com.project.skin_me.dto.UserDto;
import com.project.skin_me.enums.ActivityType;
import com.project.skin_me.exception.AlreadyExistsException;
import com.project.skin_me.exception.ResourceNotFoundException;
import com.project.skin_me.model.Activity;
import com.project.skin_me.model.Role;
import com.project.skin_me.model.User;
import com.project.skin_me.repository.ActivityRepository;
import com.project.skin_me.repository.RoleRepository;
import com.project.skin_me.repository.UserRepository;
import com.project.skin_me.request.CreateUserRequest;
import com.project.skin_me.request.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUserById(Long userId) {
        logger.debug("Fetching user by ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });
    }

    @Override
    public List<UserDto> getAllUsers() {
        logger.debug("Fetching all users");
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            logger.info("No users found in the database");
        } else {
            logger.info("Found {} users", users.size());
        }
        return users.stream()
                .map(this::convertUserToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public User createUser(CreateUserRequest request) {
        logger.debug("Creating user with email: {}", request.getEmail());
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            logger.warn("Passwords do not match for email: {}", request.getEmail());
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("User already exists with email: {}", request.getEmail());
            throw new AlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> {
                    logger.error("Default role ROLE_USER not found");
                    return new RuntimeException("Default role ROLE_USER not found.");
                });

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setConfirmPassword(null);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setRegistrationDate(LocalDateTime.now());
        user.setIsOnline(false);
        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        User savedUser = userRepository.save(user);

        Activity activity = new Activity();
        activity.setUser(savedUser);
        activity.setActivityType(ActivityType.REGISTER);
        activity.setTimestamp(LocalDateTime.now());
        activity.setDetails("User registered with email: " + user.getEmail());
        activityRepository.save(activity);

        logger.info("User created successfully: {}", request.getEmail());
        return savedUser;
    }

    @Override
    @Transactional
    public User updateUser(UserUpdateRequest request, Long userId) {
        logger.debug("Updating user with ID: {}", userId);
        return userRepository.findById(userId).map(existingUser -> {
            existingUser.setFirstName(request.getFirstName());
            existingUser.setLastName(request.getLastName());
            User updatedUser = userRepository.save(existingUser);
            logger.info("User updated successfully: ID {}", userId);
            return updatedUser;
        }).orElseThrow(() -> {
            logger.error("User not found with ID: {}", userId);
            return new ResourceNotFoundException("User not found with ID: " + userId);
        });
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        logger.debug("Deleting user with ID: {}", userId);
        userRepository.findById(userId)
                .ifPresentOrElse(user -> {
                    userRepository.delete(user);
                    logger.info("User deleted successfully: ID {}", userId);
                }, () -> {
                    logger.error("User not found with ID: {}", userId);
                    throw new ResourceNotFoundException("User not found with ID: " + userId);
                });
    }

    @Override
    public UserDto convertUserToDto(User user) {
        logger.debug("Converting user to DTO: {}", user.getEmail());
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        logger.debug("Fetching authenticated user with email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new ResourceNotFoundException("User not found with email: " + email);
                });
    }

    @Override
    @Transactional
    public void recordPurchase(Long userId, String orderDetails) {
        logger.debug("Recording purchase for user ID: {}", userId);
        if (orderDetails == null || orderDetails.trim().isEmpty()) {
            logger.warn("Invalid order details for user ID: {}", userId);
            throw new IllegalArgumentException("Order details cannot be empty");
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Activity activity = new Activity();
            activity.setUser(user);
            activity.setActivityType(ActivityType.PURCHASE);
            activity.setTimestamp(LocalDateTime.now());
            activity.setDetails("Purchase made: " + orderDetails);
            activityRepository.save(activity);

            logger.info("Purchase recorded for user ID: {}", userId);
        } else {
            logger.error("User not found with ID: {}", userId);
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
    }

    @Override
    public List<Activity> getUserActivityHistory(Long userId) {
        logger.debug("Fetching activity history for user ID: {}", userId);
        return activityRepository.findByUserId(userId);
    }

    @Override
    public boolean isUserOnline(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        boolean online = userOptional.map(User::isOnline).orElse(false);
        logger.debug("User ID: {} isOnline: {}", userId, online);
        return online;
    }
}