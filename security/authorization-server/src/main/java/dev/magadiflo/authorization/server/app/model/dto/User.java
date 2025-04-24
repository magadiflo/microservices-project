package dev.magadiflo.authorization.server.app.model.dto;

import java.util.Set;

public record User(Long id,
                   String username,
                   String password,
                   boolean enabled,
                   String email,
                   Set<Role> roles) {
}
