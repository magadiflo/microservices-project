package dev.magadiflo.user.app.model.dto;

public record UserRequest(String username,
                          String password,
                          String email) {
}
