package dev.magadiflo.user.app.util;

import dev.magadiflo.user.app.entity.User;
import dev.magadiflo.user.app.model.dto.UserRequest;
import dev.magadiflo.user.app.model.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.isEnabled(), user.getEmail());
    }

    public User toUser(UserRequest request) {
        return User.builder()
                .username(request.username())
                .password(request.password())
                .email(request.email())
                .build();
    }

    public User toUpdateUser(User user, UserRequest request) {
        user.setUsername(request.username());
        user.setEmail(request.email());
        return user;
    }
}
