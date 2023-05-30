package hexlet.code.domain.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * DTO for {@link User}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserOperationDto implements Serializable {

    @Size(message = "First name cannot exceed 50 characters", max = 50)
    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @Size(message = "Last name cannot exceed 50 characters", max = 50)
    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @Size(message = "Email cannot exceed 100 characters", max = 100)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @ToString.Exclude
    @Size(message = "Password must be between 3 and 100 characters", min = 3, max = 100)
    @NotBlank(message = "Password cannot be blank")
    private String password;
}
