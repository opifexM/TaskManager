package hexlet.code.domain.status;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link Status}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusOperationDto implements Serializable {
    @Size(message = "Status name must be between 1 and 50 characters", min = 1, max = 50)
    @NotBlank(message = "Status name cannot be blank")
    private String name;
}
