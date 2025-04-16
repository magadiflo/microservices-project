package dev.magadiflo.user.app.service;

import dev.magadiflo.user.app.model.dto.UserEnabledRequest;
import dev.magadiflo.user.app.model.dto.UserRequest;
import dev.magadiflo.user.app.model.dto.UserResponse;

import java.util.List;
import java.util.Set;

public interface UserService {
    List<UserResponse> findUsers();

    UserResponse findUser(Long userId);

    UserResponse findUserByUsername(String username);

    UserResponse saveUser(UserRequest userRequest);

    UserResponse updateUser(Long userId, UserRequest userRequest);

    void deleteUser(Long userId);

    UserResponse updateUserEnabled(Long userId, UserEnabledRequest userEnabledRequest);

    UserResponse updateUserRoles(Long userId, Set<String> roleNames);
}
