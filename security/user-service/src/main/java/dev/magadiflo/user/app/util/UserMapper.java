package dev.magadiflo.user.app.util;

import dev.magadiflo.user.app.entity.User;
import dev.magadiflo.user.app.model.dto.UserEnabledRequest;
import dev.magadiflo.user.app.model.dto.UserRequest;
import dev.magadiflo.user.app.model.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.isEnabled(), user.getEmail());
    }

    public User toUser(UserRequest request) {
        return User.builder()
                .username(request.username())
                .password(this.passwordEncoder.encode(request.password()))
                .enabled(true)
                .email(request.email())
                .build();
    }

    public User toUpdateUser(User user, UserRequest request) {
        user.setUsername(request.username());
        user.setEmail(request.email());
        return user;
    }

    public User toUpdateUserEnabled(User user, UserEnabledRequest userEnabledRequest) {
        user.setEnabled(userEnabledRequest.enabled());
        return user;
    }
}
