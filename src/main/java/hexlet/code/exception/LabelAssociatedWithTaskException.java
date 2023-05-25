package hexlet.code.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class LabelAssociatedWithTaskException extends RuntimeException {
    public LabelAssociatedWithTaskException(final String message) {
        super(message);
    }
}

