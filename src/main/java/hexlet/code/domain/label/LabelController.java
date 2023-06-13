package hexlet.code.domain.label;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("${base-url}" + "/labels")
@Tag(name = "Label Management", description = "Label management API")
@RequiredArgsConstructor
@Slf4j
public class LabelController {
    private final LabelMapper labelMapper;

    private final LabelService labelService;

    @GetMapping("")
    @Operation(summary = "List all labels", description = "Retrieves all labels")
    public List<LabelDto> getAllLabels() {
        log.info("Listing all labels");
        return labelService.findAll().stream().map(labelMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get label by ID", description = "Retrieves a label by ID")
    public LabelDto getLabel(@PathVariable("id") @Parameter(description = "Label ID") final long id) {
        log.info("Getting label with ID: {}", id);
        return labelMapper.toDto(labelService.findById(id));
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new label", description = "Creates a new label")
    public LabelDto createLabel(
            @Valid @RequestBody @Parameter(description = "Label object") final LabelOperationDto labelOperationDto) {
        log.info("Creating a new label: {}", labelOperationDto);
        Label labelToCreate = labelMapper.toEntity(labelOperationDto);
        Label savedLabel = labelService.save(labelToCreate);
        return labelMapper.toDto(savedLabel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update label by ID", description = "Updates label information by ID")
    public LabelDto updateLabel(
            @Valid @RequestBody
            @Parameter(description = "Updated label object") final LabelOperationDto labelOperationDto,
            @PathVariable("id")
            @Parameter(description = "Label ID") final long id) {
        log.info("Updating label with ID: {} with data: {}", id, labelOperationDto);
        Label labelToUpdate = labelMapper.toEntity(labelOperationDto);
        Label updatedLabel = labelService.updateById(labelToUpdate, id);
        return labelMapper.toDto(updatedLabel);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete label by ID", description = "Deletes a label by ID")
    public void deleteLabel(@PathVariable("id") @Parameter(description = "Label ID") final long id) {
        log.info("Deleting label with ID: {}", id);
        labelService.deleteById(id);
    }
}
