package hexlet.code.domain.task;

import hexlet.code.domain.status.StatusDto;
import hexlet.code.domain.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link Task}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDto implements Serializable {
    Long id;
    String name;
    String description;
    StatusDto taskStatus;
    UserDto author;
    UserDto executor;
    LocalDateTime createdAt;
}