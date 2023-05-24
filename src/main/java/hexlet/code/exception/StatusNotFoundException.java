package hexlet.code.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class StatusNotFoundException extends RuntimeException {
    public StatusNotFoundException(final String message) { super(message); }
}


