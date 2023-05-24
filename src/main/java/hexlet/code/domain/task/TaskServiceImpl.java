package hexlet.code.domain.task;

import hexlet.code.domain.exception.TaskNotFoundException;
import hexlet.code.domain.status.Status;
import hexlet.code.domain.status.StatusService;
import hexlet.code.domain.user.User;
import hexlet.code.domain.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.nonNull;

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final StatusService statusService;

    public TaskServiceImpl(TaskRepository taskRepository, UserService userService, StatusService statusService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.statusService = statusService;
    }

    @Override
    public List<Task> findAll() {
        log.info("Retrieving all tasks");
        return taskRepository.findAll();
    }

    @Override
    public Task findById(Long id) {
        log.info("Retrieving task with ID: {}", id);
        return taskRepository.findById(id)
                .orElseThrow(() -> TaskNotFoundException.forId(id));
    }

    @Override
    public Task save(Task newTask) {
        log.info("Saving new task: {}", newTask);
        Task savedTask = taskRepository.save(newTask);
        log.info("Successfully saved new status: {}", savedTask);
        return savedTask;
    }

    @Override
    public Task createTask(TaskCreationDto taskCreationDto) {
        Status status = statusService.findById(taskCreationDto.getTaskStatusId());
        User author = userService.findById(taskCreationDto.getAuthorId());

        Task taskToCreate = new Task();
        taskToCreate.setName(taskCreationDto.getName());
        taskToCreate.setDescription(taskCreationDto.getDescription());
        taskToCreate.setTaskStatus(status);
        taskToCreate.setAuthor(author);
        if (nonNull(taskCreationDto.getExecutorId())) {
            User executor = userService.findById(taskCreationDto.getExecutorId());
            taskToCreate.setExecutor(executor);
        }
        return save(taskToCreate);
    }

    @Override
    public Task updateTask(TaskChangingDto taskChangingDto, long id) {
        Task taskToUpdate = findById(id);
        Status status = statusService.findById(taskChangingDto.getTaskStatusId());
        User author = userService.findById(taskChangingDto.getAuthorId());
        taskToUpdate.setName(taskChangingDto.getName());
        taskToUpdate.setDescription(taskChangingDto.getDescription());
        taskToUpdate.setTaskStatus(status);
        taskToUpdate.setAuthor(author);
        if (nonNull(taskChangingDto.getExecutorId())) {
            User executor = userService.findById(taskChangingDto.getExecutorId());
            taskToUpdate.setExecutor(executor);
        }
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
                    return taskRepository.save(task);
                })
                .orElseThrow(() -> TaskNotFoundException.forId(id));
        log.info("Successfully updated task {}", savedTask);
        return savedTask;
    }

    @Override
    public void deleteById(long id) {
        log.info("Deleting task with ID: {}", id);
        if (!taskRepository.existsById(id)) {
            throw TaskNotFoundException.forId(id);
        }
        taskRepository.deleteById(id);
        log.info("Successfully deleted task with ID: {}", id);
    }

}
