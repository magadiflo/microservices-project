package dev.magadiflo.authorization.server.app.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthConstant {
    public static final String NO_FOUND_MESSAGE = "Login error. The user with id %s does not exist in user-service";
    public static final String COMMUNICATION_MESSAGE = "An error occurred in the user-service: %s";
}
