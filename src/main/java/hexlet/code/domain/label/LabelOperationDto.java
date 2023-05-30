package hexlet.code.domain.label;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link Label}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LabelOperationDto implements Serializable {
    @Size(message = "Label name must be between 1 and 50 characters", min = 1, max = 50)
    @NotBlank(message = "Label name cannot be blank")
    private String name;
}
