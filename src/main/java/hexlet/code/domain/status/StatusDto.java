package hexlet.code.domain.status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link Status}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusDto implements Serializable {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}
