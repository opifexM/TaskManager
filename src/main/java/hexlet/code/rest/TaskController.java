package hexlet.code.rest;

import hexlet.code.domain.status.Status;
import hexlet.code.domain.status.StatusService;
import hexlet.code.domain.task.Task;
import hexlet.code.domain.task.TaskChangingDto;
import hexlet.code.domain.task.TaskCreationDto;
import hexlet.code.domain.task.TaskDto;
import hexlet.code.domain.task.TaskMapper;
import hexlet.code.domain.task.TaskService;
import hexlet.code.domain.user.User;
import hexlet.code.domain.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.Objects.nonNull;

@RestController
@RequestMapping("${base-url}" + "/tasks")
@Tag(name = "Task Management", description = "Task management API")
@Slf4j
public class TaskController {
    private final TaskMapper taskMapper;

    private final TaskService taskService;
    private final StatusService statusService;
    private final UserService userService;


    public TaskController(TaskMapper taskMapper, TaskService taskService,
                          StatusService statusService, UserService userService) {
        this.taskMapper = taskMapper;
        this.taskService = taskService;
        this.statusService = statusService;
        this.userService = userService;
    }

    @GetMapping("")
    @Operation(summary = "List all tasks", description = "Retrieves all tasks")
    public List<TaskDto> listAllTasks() {
        log.info("Listing all tasks");
        return taskService.findAll().stream().map(taskMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieves a task by ID")
    public TaskDto getTaskById(@PathVariable("id") @Parameter(description = "Task ID") final long id) {
        log.info("Getting task with ID: {}", id);
        return taskMapper.toDto(taskService.findById(id));
    }

    @PostMapping("")
    @Operation(summary = "Create a new task", description = "Creates a new task")
    public TaskDto createTask(
            @Valid @RequestBody @Parameter(description = "Task object") final TaskCreationDto taskCreationDto) {
        log.info("Creating a new task: {}", taskCreationDto);

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

        Task savedTask = taskService.save(taskToCreate);
        return taskMapper.toDto(savedTask);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task by ID", description = "Updates task information by ID")
    public TaskDto updateTask(
            @Valid @RequestBody @Parameter(description = "Updated task object") final TaskChangingDto taskChangingDto,
            @PathVariable("id") @Parameter(description = "Task ID") final long id) {
        log.info("Updating task with ID: {} with data: {}", id, taskChangingDto);

        Task taskToUpdate = taskService.findById(id);
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

        Task updatedTask = taskService.updateById(taskToUpdate, id);
        return taskMapper.toDto(updatedTask);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete task by ID", description = "Deletes a task by ID")
    public void deleteTask(@PathVariable("id") @Parameter(description = "Task ID") final long id) {
        log.info("Deleting task with ID: {}", id);
        taskService.deleteById(id);
    }
}
