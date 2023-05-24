package hexlet.code.domain.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link Task}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskCreationDto implements Serializable {
    @Size(message = "Task name must be between 1 and 255 characters", min = 1, max = 255)
    @NotBlank(message = "Task name cannot be blank")
    String name;

    String description;

    @NotNull(message = "Task status cannot be Null")
    Long taskStatusId;

    @NotNull(message = "Task author cannot be Null")
    Long authorId;

    Long executorId;
}