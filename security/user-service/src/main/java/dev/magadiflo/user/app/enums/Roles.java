package dev.magadiflo.user.app.enums;

import java.util.Arrays;

public enum Roles {
    ROLE_USER,
    ROLE_ADMIN;

    public static boolean exists(String roleName) {
        return Arrays.stream(Roles.values())
                .anyMatch(role -> role.name().equals(roleName));
    }
}
