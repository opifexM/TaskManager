package hexlet.code.domain.task;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("${base-url}" + "/tasks")
@Tag(name = "Task Management", description = "Task management API")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    private final TaskMapper taskMapper;

    private final TaskService taskService;

    @GetMapping("")
    @Operation(summary = "List all tasks", description = "Retrieves all tasks")
    @Transactional
    public List<TaskDto> getAllTasks(
            @RequestParam(required = false) @Parameter(description = "Task Status ID") Long taskStatus,
            @RequestParam(required = false) @Parameter(description = "Executor ID") Long executorId,
            @RequestParam(required = false) @Parameter(description = "Label ID") Long labelsId,
            @RequestParam(required = false) @Parameter(description = "Author ID") Long authorId
    ) {
        log.info("Listing all tasks");
        return taskService.findAll(
                        Optional.ofNullable(taskStatus),
                        Optional.ofNullable(executorId),
                        Optional.ofNullable(labelsId),
                        Optional.ofNullable(authorId))
                .stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieves a task by ID")
    @Transactional
    public TaskDto getTask(@PathVariable("id") @Parameter(description = "Task ID") final long id) {
        log.info("Getting task with ID: {}", id);
        return taskMapper.toDto(taskService.findById(id));
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new task", description = "Creates a new task")
    @Transactional
    public TaskDto createTask(
            @Valid @RequestBody @Parameter(description = "Task object") final TaskOperationDto taskOperationDto) {
        log.info("Creating a new task: {}", taskOperationDto);

        Task taskToCreate = taskMapper.toEntity(taskOperationDto);
        @SuppressWarnings("DataFlowIssue")
        Optional<Long> taskStatusId = Optional.ofNullable(taskOperationDto.getTaskStatusId());
        Optional<Long> executorId = Optional.ofNullable(taskOperationDto.getExecutorId());
        Optional<Set<Long>> labelIds = Optional.ofNullable(taskOperationDto.getLabelIds());
        Task savedTask = taskService.configureAndSaveTask(taskToCreate, taskStatusId, executorId, labelIds);
        return taskMapper.toDto(savedTask);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task by ID", description = "Updates task information by ID")
    public TaskDto updateTask(
            @Valid @RequestBody @Parameter(description = "Updated task object") final TaskOperationDto taskOperationDto,
            @PathVariable("id") @Parameter(description = "Task ID") final long id) {
        log.info("Updating task with ID: {} with data: {}", id, taskOperationDto);

        Task taskToUpdate = taskMapper.toEntity(taskOperationDto);
        long taskStatusId = Optional.of(taskOperationDto.getTaskStatusId()).orElse(0L);
        long executorId = Optional.ofNullable(taskOperationDto.getExecutorId()).orElse(0L);
        Set<Long> labelIds = Optional.ofNullable(taskOperationDto.getLabelIds()).orElse(new HashSet<>());

        Task updatedTask = taskService.updateTask(taskToUpdate, taskStatusId, executorId, labelIds, id);
        return taskMapper.toDto(updatedTask);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task by ID", description = "Deletes a task by ID")
    @PreAuthorize("@userSecurityService.isTaskOwner(#id)")
    public void deleteTask(@PathVariable("id") @Parameter(description = "Task ID") final long id) {
        log.info("Deleting task with ID: {}", id);
        taskService.deleteById(id);
    }
}
