package hexlet.code.rest;

import hexlet.code.domain.user.User;
import hexlet.code.domain.user.UserDto;
import hexlet.code.domain.user.UserMapper;
import hexlet.code.domain.user.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/users")
@Slf4j
public class UserController {
    private final UserMapper userMapper;

    private final UserService userService;

    @Autowired
    public UserController(UserService userService,
                          UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("")
    public List<UserDto> listAllUsers() {
        return userService.findAll().stream().map(userMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable("id") long id) {
       return userMapper.toDto(userService.findById(id));
    }

    @PostMapping("")
    public UserDto createUser(@Valid @RequestBody User newUser) {
        return userMapper.toDto(userService.save(newUser));
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@Valid @RequestBody User updatedUser,
                              @PathVariable("id") long id) {
        return userMapper.toDto(userService.updateById(updatedUser, id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable("id") long id) {
        userService.deleteById(id);
    }
}
