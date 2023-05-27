package hexlet.code.domain.status;

import hexlet.code.domain.task.TaskRepository;
import hexlet.code.exception.DuplicateStatusException;
import hexlet.code.exception.StatusAssociatedWithTaskException;
import hexlet.code.exception.StatusNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusServiceImpl implements StatusService {

    private final StatusRepository statusRepository;
    private final TaskRepository taskRepository;

    @Override
    public List<Status> findAll() {
        log.info("Retrieving all statuses");
        return statusRepository.findAll();
    }

    @Override
    public Status findById(Long id) {
        log.info("Retrieving status with ID: {}", id);
        return statusRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Failed to retrieve status. Status with ID %d not found.", id);
                    log.error(message);
                    return new StatusNotFoundException(message);
                });
    }

    @Override
    public Status save(Status newStatus) {
        log.info("Saving new status: {}", newStatus);
        if (statusRepository.findByName(newStatus.getName()).isPresent()) {
            String message = String.format("Failed to save status. Status with name '%s' already exists.",
                    newStatus.getName());
            log.error(message);
            throw new DuplicateStatusException(message);
        }
        Status savedStatus = statusRepository.save(newStatus);
        log.info("Successfully saved new status: {}", savedStatus);
        return savedStatus;
    }

    @Override
    public Status updateById(Status updatedStatus, long id) {
        log.info("Updating status with ID: {} with data: {}", id, updatedStatus);
        Status savedStatus = statusRepository.findById(id)
                .map(status -> {
                    status.setName(updatedStatus.getName());
                    return statusRepository.save(status);
                })
                .orElseThrow(() -> {
                    String message = String.format("Failed to update status. Status with ID %d not found.", id);
                    log.error(message);
                    return new StatusNotFoundException(message);
                });
        log.info("Successfully updated status {}", savedStatus);
        return savedStatus;
    }

    @Override
    public void deleteById(long id) {
        log.info("Deleting status with ID: {}", id);
        if (!statusRepository.existsById(id)) {
            String message = String.format("Failed to delete status. Status with ID %d not found.", id);
            log.error(message);
            throw new StatusNotFoundException(message);
        }
        if (taskRepository.existsByTaskStatus_Id(id)) {
            String message = String.format("Status with ID %d is associated with a task, cannot delete.", id);
            log.error(message);
            throw new StatusAssociatedWithTaskException(message);
        }
        statusRepository.deleteById(id);
        log.info("Successfully deleted status with ID: {}", id);
    }
}
