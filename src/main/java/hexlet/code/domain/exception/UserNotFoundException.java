package hexlet.code.domain.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserNotFoundException extends RuntimeException {
    private static final String MESSAGE_NO_USER_WITH_ID = "User with id %d not found";
    private static final String MESSAGE_NO_USER_WITH_EMAIL = "User with email %s not found";

    public UserNotFoundException(final String message) {
        super(message);
    }

    public static UserNotFoundException forId(final long id) {
        final String message = String.format(MESSAGE_NO_USER_WITH_ID, id);
        log.error(message);
        return new UserNotFoundException(message);
    }

    public static UserNotFoundException forEmail(final String email) {
        final String message = String.format(MESSAGE_NO_USER_WITH_EMAIL, email);
        log.error(message);
        return new UserNotFoundException(message);
    }
}

