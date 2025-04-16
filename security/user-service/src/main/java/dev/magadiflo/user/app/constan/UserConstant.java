package dev.magadiflo.user.app.constan;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserConstant {
    public static final String NO_SUCH_ELEMENT_MESSAGE = "The user with id %d does not exist";
    public static final String NO_SUCH_USERNAME_MESSAGE = "The user with username %s does not exist";
    public static final String ILLEGAL_STATE_EXCEPTION = "There is an error with the user with id %d";
    public static final String ILLEGAL_STATE_ROLE_EXCEPTION = "One or more roles are invalid";
}
