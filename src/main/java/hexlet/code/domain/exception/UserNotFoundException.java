package hexlet.code.domain.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserNotFoundException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "User with id %d not found";

    public UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException forId(long id) {
        String message = String.format(MESSAGE_TEMPLATE, id);
        log.error(message);
        return new UserNotFoundException(message);
    }
}

