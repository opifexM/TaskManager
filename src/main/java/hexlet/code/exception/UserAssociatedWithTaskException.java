package hexlet.code.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class UserAssociatedWithTaskException extends RuntimeException {
    public UserAssociatedWithTaskException(final String message) {
        super(message);
    }
}

