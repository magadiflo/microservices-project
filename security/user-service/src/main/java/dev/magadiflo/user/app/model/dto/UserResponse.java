package dev.magadiflo.user.app.model.dto;

import java.util.Set;

public record UserResponse(Long id,
                           String username,
                           boolean enabled,
                           String email,
                           Set<RoleResponse> roles) {
}
