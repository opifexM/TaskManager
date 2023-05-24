package hexlet.code.domain.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatusNotFoundException extends RuntimeException {
    private static final String STATUS_WITH_ID_NOT_FOUND = "Status with id %d not found";

    public StatusNotFoundException(final String message) {
        super(message);
    }

    public static StatusNotFoundException forId(final long id) {
        final String message = String.format(STATUS_WITH_ID_NOT_FOUND, id);
        log.error(message);
        return new StatusNotFoundException(message);
    }
}

