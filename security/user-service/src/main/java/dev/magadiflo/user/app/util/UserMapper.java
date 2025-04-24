package dev.magadiflo.user.app.util;

import dev.magadiflo.user.app.entity.Role;
import dev.magadiflo.user.app.entity.User;
import dev.magadiflo.user.app.model.dto.RoleResponse;
import dev.magadiflo.user.app.model.dto.UserEnabledRequest;
import dev.magadiflo.user.app.model.dto.UserRequest;
import dev.magadiflo.user.app.model.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public UserResponse toUserResponse(User user) {
        Set<RoleResponse> rolesResponse = user.getRoles().stream()
                .map(role -> new RoleResponse(role.getId(), role.getName()))
                .collect(Collectors.toSet());
        return new UserResponse(user.getId(), user.getUsername(), user.getPassword(), user.isEnabled(), user.getEmail(), rolesResponse);
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

    public User toUpdateUserRoles(User user, Set<Role> roles) {
        user.setRoles(roles);
        return user;
    }
}
