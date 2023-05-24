package hexlet.code.rest;

import hexlet.code.domain.status.Status;
import hexlet.code.domain.status.StatusChangingDto;
import hexlet.code.domain.status.StatusCreationDto;
import hexlet.code.domain.status.StatusDto;
import hexlet.code.domain.status.StatusMapper;
import hexlet.code.domain.status.StatusService;
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

@RestController
@RequestMapping("${base-url}" + "/statuses")
@Tag(name = "Status Management", description = "Status management API")
@Slf4j
public class StatusController {
    private final StatusMapper statusMapper;

    private final StatusService statusService;

    public StatusController(StatusMapper statusMapper, StatusService statusService) {
        this.statusMapper = statusMapper;
        this.statusService = statusService;
    }

    @GetMapping("")
    @Operation(summary = "List all statuses", description = "Retrieves all statuses")
    public List<StatusDto> listAllStatuses() {
        log.info("Listing all statuses");
        return statusService.findAll().stream().map(statusMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get status by ID", description = "Retrieves a status by ID")
    public StatusDto getStatusById(@PathVariable("id") @Parameter(description = "Status ID") final long id) {
        log.info("Getting status with ID: {}", id);
        return statusMapper.toDto(statusService.findById(id));
    }

    @PostMapping("")
    @Operation(summary = "Create a new status", description = "Creates a new status")
    public StatusDto createStatus(
            @Valid @RequestBody @Parameter(description = "Status object") final StatusCreationDto statusCreationDto) {
        log.info("Creating a new status: {}", statusCreationDto);
        Status statusToCreate = statusMapper.toEntity(statusCreationDto);
        Status savedStatus = statusService.save(statusToCreate);
        return statusMapper.toDto(savedStatus);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update status by ID", description = "Updates status information by ID")
    public StatusDto updateStatus(
            @Valid @RequestBody @Parameter(description = "Updated status object") final StatusChangingDto statusChangingDto,
            @PathVariable("id") @Parameter(description = "Status ID") final long id) {
        log.info("Updating status with ID: {} with data: {}", id, statusChangingDto);
        Status statusToUpdate = statusMapper.toEntity(statusChangingDto);
        Status updatedStatus = statusService.updateById(statusToUpdate, id);
        return statusMapper.toDto(updatedStatus);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete status by ID", description = "Deletes a status by ID")
    public void deleteStatus(@PathVariable("id") @Parameter(description = "Status ID") final long id) {
        log.info("Deleting status with ID: {}", id);
        statusService.deleteById(id);
    }
}
