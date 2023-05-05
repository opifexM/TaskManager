package hexlet.code.domain.exception;

public class UserNotFoundException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "User with id %d not found";

    public UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException forId(long id) {
        return new UserNotFoundException(String.format(MESSAGE_TEMPLATE, id));
    }
}

