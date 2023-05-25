package hexlet.code.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class StatusAssociatedWithTaskException extends RuntimeException {
    public StatusAssociatedWithTaskException(final String message) {
        super(message);
    }
}

