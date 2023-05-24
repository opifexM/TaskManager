package hexlet.code.domain.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskNotFoundException extends RuntimeException {
    private static final String TASK_WITH_ID_NOT_FOUND = "Task with id %d not found";

    public TaskNotFoundException(final String message) {
        super(message);
    }

    public static TaskNotFoundException forId(final long id) {
        final String message = String.format(TASK_WITH_ID_NOT_FOUND, id);
        log.error(message);
        return new TaskNotFoundException(message);
    }
}

