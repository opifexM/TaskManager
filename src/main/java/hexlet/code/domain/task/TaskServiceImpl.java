package hexlet.code.domain.task;

import hexlet.code.domain.label.Label;
import hexlet.code.domain.label.LabelService;
import hexlet.code.domain.status.StatusService;
import hexlet.code.domain.user.UserService;
import hexlet.code.exception.DuplicateTaskException;
import hexlet.code.exception.TaskNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final StatusService statusService;
    private final UserService userService;
    private final LabelService labelService;

    @Override
    public List<Task> findAll(Optional<Long> taskStatusId, Optional<Long> executorId,
                              Optional<Long> labelsId, Optional<Long> authorId) {
        log.info("Received filter parameters: taskStatusId={}, executorId={}, labelsId={}, authorId={}",
                taskStatusId, executorId, labelsId, authorId);
        List<Task> tasks = taskRepository.findAll(
                Specification.where(taskStatusId.map(TaskSpecifications::hasTaskStatus).orElse(null))
                        .and(executorId.map(TaskSpecifications::hasExecutor).orElse(null))
                        .and(labelsId.map(TaskSpecifications::hasLabel).orElse(null))
                        .and(authorId.map(TaskSpecifications::hasAuthor).orElse(null))
        );

        log.info("Found {} tasks with the provided filter parameters.", tasks);
        return tasks;
    }

    @Override
    public Task findById(Long id) {
        log.info("Retrieving task with ID: {}", id);
        return taskRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Failed to retrieve task. Task with ID %d not found.", id);
                    log.error(message);
                    return new TaskNotFoundException(message);
                });
    }

    @Override
    public Task save(Task newTask) {
        log.info("Saving new task: {}", newTask);
        if (taskRepository.findByName(newTask.getName()).isPresent()) {
            String message = String.format("Failed to save task. Task with name '%s' already exists.",
                    newTask.getName());
            log.error(message);
            throw new DuplicateTaskException(message);
        }
        Task savedTask = taskRepository.save(newTask);
        log.info("Successfully saved new status: {}", savedTask);
        return savedTask;
    }

    @Override
    public Task configureAndSaveTask(Task newTask, Optional<Long> taskStatusId,
                                     Optional<Long> executorId, Optional<Set<Long>> labelIds) {

        long currentUserNameId = userService.getCurrentUserNameId();
        newTask.setAuthor(userService.findById(currentUserNameId));
        taskStatusId.ifPresent(id -> newTask.setTaskStatus(statusService.findById(id)));
        executorId.ifPresent(id -> newTask.setExecutor(userService.findById(id)));
        labelIds.ifPresent(ids -> {
            Set<Label> labelSet = ids.stream()
                    .map(labelService::findById)
                    .collect(Collectors.toSet());
            newTask.setLabels(labelSet);
        });
        return save(newTask);
    }

    @Override
    public Task updateTask(Task taskToUpdate, long taskStatusId,
                           long executorId, Set<Long> labelIds, long id) {

        long currentUserNameId = userService.getCurrentUserNameId();
        taskToUpdate.setAuthor(userService.findById(currentUserNameId));
        taskToUpdate.setTaskStatus(statusService.findById(taskStatusId));
        if (executorId > 0) {
            taskToUpdate.setExecutor(userService.findById(executorId));
        }
        Set<Label> labelSet = labelIds.stream()
                .map(labelService::findById)
                .collect(Collectors.toSet());
        taskToUpdate.setLabels(labelSet);
        return updateById(taskToUpdate, id);
    }

    @Override
    public Task updateById(Task updatedTask, long id) {
        log.info("Updating task with ID: {} with data: {}", id, updatedTask);
        Task savedTask = taskRepository.findById(id)
                .map(task -> {
                    task.setName(updatedTask.getName());
                    task.setDescription(updatedTask.getDescription());
                    task.setTaskStatus(updatedTask.getTaskStatus());
                    task.setAuthor(updatedTask.getAuthor());
                    task.setExecutor(updatedTask.getExecutor());
                    task.setLabels(updatedTask.getLabels());
                    return taskRepository.save(task);
                })
                .orElseThrow(() -> {
                    String message = String.format("Failed to update task. Task with ID %d not found.", id);
                    log.error(message);
                    return new TaskNotFoundException(message);
                });
        log.info("Successfully updated task {}", savedTask);
        return savedTask;
    }

    @Override
    public void deleteById(long id) {
        log.info("Deleting task with ID: {}", id);
        if (!taskRepository.existsById(id)) {
            String message = String.format("Failed to delete task. Task with ID %d not found.", id);
            log.error(message);
            throw new TaskNotFoundException(message);
        }
        taskRepository.deleteById(id);
        log.info("Successfully deleted task with ID: {}", id);
    }
}
