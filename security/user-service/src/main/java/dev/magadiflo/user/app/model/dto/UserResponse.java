package dev.magadiflo.user.app.model.dto;

public record UserResponse(Long id,
                           String username,
                           boolean enabled,
                           String email) {
}
