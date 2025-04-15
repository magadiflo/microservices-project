package dev.magadiflo.user.app.service;

import dev.magadiflo.user.app.model.dto.UserRequest;
import dev.magadiflo.user.app.model.dto.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> findUsers();

    UserResponse findUser(Long userId);

    UserResponse findUserByUsername(String username);

    UserResponse saveUser(UserRequest userRequest);

    UserResponse updateUser(Long userId, UserRequest userRequest);

    void deleteUser(Long userId);
}
