package com.project.skin_me.service.user;

import com.project.skin_me.dto.UserDto;
import com.project.skin_me.model.User;
import com.project.skin_me.request.CreateUserRequest;
import com.project.skin_me.request.UserUpdateRequest;

public interface IUserService {

    User getUserById(Long userId);
    User createUser(CreateUserRequest request);
    User updateUser(UserUpdateRequest request, Long UserId);
    void deleteUser(Long UserId);

    UserDto convertUserToDto(User user);

    User getAuthenticatedUser();
}
