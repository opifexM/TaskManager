package hexlet.code.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(final String message) {
        super(message);
    }
}

