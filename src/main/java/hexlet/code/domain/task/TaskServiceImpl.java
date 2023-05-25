package hexlet.code.domain.task;

import hexlet.code.domain.label.Label;
import hexlet.code.domain.label.LabelService;
import hexlet.code.domain.status.StatusService;
import hexlet.code.domain.user.UserService;
import hexlet.code.exception.DuplicateTaskException;
import hexlet.code.exception.TaskNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public List<Task> findAll() {
        log.info("Retrieving all tasks");
        return taskRepository.findAll();
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
            String message = String.format("Failed to save task. Task with name '%s' already exists.", newTask.getName());
            log.error(message);
            throw new DuplicateTaskException(message);
        }
        Task savedTask = taskRepository.save(newTask);
        log.info("Successfully saved new status: {}", savedTask);
        return savedTask;
    }

    @Override
    public Task createTask(Task newTask, long taskStatusId, long authorId, long executorId, Set<Long> labelIds) {
        newTask.setTaskStatus(statusService.findById(taskStatusId));
        newTask.setAuthor(userService.findById(authorId));
        if (executorId > 0) {
            newTask.setExecutor(userService.findById(executorId));
        }
        Set<Label> labelSet = labelIds.stream()
                .map(labelService::findById)
                .collect(Collectors.toSet());
        newTask.setLabels(labelSet);
        return save(newTask);
    }

    @Override
    public Task updateTask(Task taskToUpdate, long taskStatusId, long authorId,
                           long executorId, Set<Long> labelIds, long id) {
        taskToUpdate.setTaskStatus(statusService.findById(taskStatusId));
        taskToUpdate.setAuthor(userService.findById(authorId));
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
