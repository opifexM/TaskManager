package hexlet.code.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class DuplicateStatusException extends RuntimeException {
    public DuplicateStatusException(final String message) {
        super(message);
    }
}

