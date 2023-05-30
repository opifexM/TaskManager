package hexlet.code.domain.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link Task}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskOperationDto implements Serializable {
    @Size(message = "Task name must be between 1 and 255 characters", min = 1, max = 255)
    @NotBlank(message = "Task name cannot be blank")
    private String name;

    private String description;

    @NotNull(message = "Task status cannot be Null")
    private Long taskStatusId;

    private Long authorId;

    private Long executorId;

    private Set<Long> labelIds;
}
