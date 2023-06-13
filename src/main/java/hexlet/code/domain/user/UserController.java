package hexlet.code.domain.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${base-url}" + "/users")
@Tag(name = "User Management", description = "User management API")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserMapper userMapper;

    private final UserService userService;
    @GetMapping("")
    @Operation(summary = "List all users", description = "Retrieves all users")
    public List<UserDto> getAllUsers() {
        log.info("Listing all users");
        return userService.findAll().stream().map(userMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by ID")
    public UserDto getUser(@PathVariable("id") @Parameter(description = "User ID") final long id) {
        log.info("Getting user with ID: {}", id);
        return userMapper.toDto(userService.findById(id));
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user", description = "Creates a new user")
    public UserDto createUser(
            @Valid @RequestBody @Parameter(description = "User object") final UserOperationDto userOperationDTO) {
        log.info("Creating a new user: {}", userOperationDTO);
        User userToCreate = userMapper.toEntity(userOperationDTO);
        User savedUser = userService.save(userToCreate);
        return userMapper.toDto(savedUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@userSecurityService.isOwner(#id)")
    @Operation(summary = "Update user by ID", description = "Updates user information by ID")
    public UserDto updateUser(
            @Valid @RequestBody @Parameter(description = "Updated user object") final UserOperationDto userOperationDto,
            @PathVariable("id") @Parameter(description = "User ID") final long id) {
        log.info("Updating user with ID: {} with data: {}", id, userOperationDto);
        User userToUpdate = userMapper.toEntity(userOperationDto);
        User updatedUser = userService.updateById(userToUpdate, id);
        return userMapper.toDto(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@userSecurityService.isOwner(#id)")
    @Operation(summary = "Delete user by ID", description = "Deletes a user by ID")
    public void deleteUser(@PathVariable("id") @Parameter(description = "User ID") final long id) {
        log.info("Deleting user with ID: {}", id);
        userService.deleteById(id);
    }
}
