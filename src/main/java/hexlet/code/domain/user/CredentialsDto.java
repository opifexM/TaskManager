package hexlet.code.domain.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * A DTO for the {@link User} entity
 */
@Data
public class CredentialsDto implements Serializable {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private final String email;
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 3, max = 100, message = "Password must be between 3 and 100 characters")
    private final String password;
}