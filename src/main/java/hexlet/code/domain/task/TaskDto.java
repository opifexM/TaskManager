package hexlet.code.domain.task;

import hexlet.code.domain.label.LabelDto;
import hexlet.code.domain.status.StatusDto;
import hexlet.code.domain.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DTO for {@link Task}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDto implements Serializable {
    private Long id;
    private String name;
    private String description;
    private StatusDto taskStatus;
    private UserDto author;
    private UserDto executor;
    private LocalDateTime createdAt;
    private Set<LabelDto> labels = new LinkedHashSet<>();
}
