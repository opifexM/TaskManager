package hexlet.code.rest;

import hexlet.code.domain.task.Task;
import hexlet.code.domain.task.TaskChangingDto;
import hexlet.code.domain.task.TaskCreationDto;
import hexlet.code.domain.task.TaskDto;
import hexlet.code.domain.task.TaskMapper;
import hexlet.code.domain.task.TaskService;
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
import java.util.Optional;

    @RestController
    @RequestMapping("${base-url}" + "/tasks")
    @Tag(name = "Task Management", description = "Task management API")
    @Slf4j
    public class TaskController {
        private final TaskMapper taskMapper;

        private final TaskService taskService;

        public TaskController(TaskMapper taskMapper, TaskService taskService) {
            this.taskMapper = taskMapper;
            this.taskService = taskService;
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

            Task taskToCreate = taskMapper.toEntity(taskCreationDto);
            long taskStatusId = Optional.of(taskCreationDto.getTaskStatusId()).orElse(0L);
            long authorId = Optional.of(taskCreationDto.getAuthorId()).orElse(0L);
            long executorId = Optional.ofNullable(taskCreationDto.getExecutorId()).orElse(0L);

            Task savedTask = taskService.createTask(taskToCreate, taskStatusId, authorId, executorId);
            return taskMapper.toDto(savedTask);
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update task by ID", description = "Updates task information by ID")
        public TaskDto updateTask(
                @Valid @RequestBody @Parameter(description = "Updated task object") final TaskChangingDto taskChangingDto,
                @PathVariable("id") @Parameter(description = "Task ID") final long id) {
            log.info("Updating task with ID: {} with data: {}", id, taskChangingDto);

            Task taskToUpdate = taskMapper.toEntity(taskChangingDto);
            long taskStatusId = Optional.of(taskChangingDto.getTaskStatusId()).orElse(0L);
            long authorId = Optional.of(taskChangingDto.getAuthorId()).orElse(0L);
            long executorId = Optional.ofNullable(taskChangingDto.getExecutorId()).orElse(0L);

            Task updatedTask = taskService.updateTask(taskToUpdate, taskStatusId, authorId, executorId, id);
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
