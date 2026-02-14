package com.project.skin_me.service.user;

import com.project.skin_me.dto.UserDto;
import com.project.skin_me.model.Activity;
import com.project.skin_me.model.User;
import com.project.skin_me.request.CreateUserRequest;
import com.project.skin_me.request.UserUpdateRequest;

import java.util.List;

public interface IUserService {
    User getUserById(Long userId);
    User createUser(CreateUserRequest request);
    User updateUser(UserUpdateRequest request, Long userId);
    void deleteUser(Long userId);
    User assignRole(Long userId, String roleName);
    User removeRole(Long userId, String roleName);
    UserDto convertUserToDto(User user);
    User getAuthenticatedUser();
    void recordPurchase(Long userId, String orderDetails);
    List<Activity> getUserActivityHistory(Long userId);
    boolean isUserOnline(Long userId);
    List<UserDto> getAllUsers();
}
