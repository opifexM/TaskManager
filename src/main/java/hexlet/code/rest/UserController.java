package hexlet.code.rest;

import hexlet.code.domain.user.User;
import hexlet.code.domain.user.UserDto;
import hexlet.code.domain.user.UserMapper;
import hexlet.code.domain.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${base-url}" + "/users")
@Tag(name = "User Management", description = "User management API")
@Slf4j
public class UserController {
    private final UserMapper userMapper;

    private final UserService userService;

    @Autowired
    public UserController(final UserService userService,
                          final UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("")
    @Operation(summary = "List all users", description = "Retrieves all users")
    public List<UserDto> listAllUsers() {
        log.info("Listing all users");
        return userService.findAll().stream().map(userMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by ID")
    public UserDto getUserById(@PathVariable("id") @Parameter(description = "User ID") final long id) {
        log.info("Getting user with ID: {}", id);
        return userMapper.toDto(userService.findById(id));
    }

    @PostMapping("")
    @Operation(summary = "Create a new user", description = "Creates a new user")
    public UserDto createUser(@Valid @RequestBody @Parameter(description = "User object") final User newUser) {
        log.info("Creating a new user: {}", newUser);
        return userMapper.toDto(userService.save(newUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@userSecurityService.isOwner(#id)")
    @Operation(summary = "Update user by ID", description = "Updates user information by ID")
    public UserDto updateUser(@Valid @RequestBody @Parameter(description = "Updated user object") final User updatedUser,
                              @PathVariable("id") @Parameter(description = "User ID") final long id) {
        log.info("Updating user with ID: {} with data: {}", id, updatedUser);
        return userMapper.toDto(userService.updateById(updatedUser, id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@userSecurityService.isOwner(#id)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user by ID", description = "Deletes a user by ID")
    public void deleteUser(@PathVariable("id") @Parameter(description = "User ID") final long id) {
        log.info("Deleting user with ID: {}", id);
        userService.deleteById(id);
    }
}
