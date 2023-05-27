package hexlet.code.domain.label;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link Label}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LabelDto implements Serializable {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}
