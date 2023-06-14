package hexlet.code.domain.user;

import hexlet.code.domain.task.TaskRepository;
import hexlet.code.exception.DuplicateUserException;
import hexlet.code.exception.UserAssociatedWithTaskException;
import hexlet.code.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TaskRepository taskRepository;

    @Override
    public List<User> findAll() {
        log.info("Retrieving all users");
        return userRepository.findAll();
    }

    @Override
    public User findById(final Long id) {
        log.info("Retrieving user with ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Failed to retrieve user. User with ID %d not found.", id);
                    log.error(message);
                    return new UserNotFoundException(message);
                });
    }

    @Override
    public User save(final User newUser) {
        log.info("Saving new user: {}", newUser);
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            String message = String.format("Failed to save user. User with email '%s' already exists.",
                    newUser.getEmail());
            log.error(message);
            throw new DuplicateUserException(message);
        }
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        User savedUser = userRepository.save(newUser);
        log.info("Successfully saved new user: {}", savedUser);
        return savedUser;
    }

    @Override
    public User updateById(final User updatedUser, final long id) {
        log.info("Updating user with ID: {} with data: {}", id, updatedUser);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Failed to update user. User with ID %d not found.", id);
                    log.error(message);
                    return new UserNotFoundException(message);
                });
        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setEmail(updatedUser.getEmail());
        user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        User savedUser = userRepository.save(user);
        log.info("Successfully updated user {}", savedUser);
        return savedUser;
    }

    @Override
    public void deleteById(final long id) {
        log.info("Deleting user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            String message = String.format("Failed to delete user. User with ID %d not found.", id);
            log.error(message);
            throw new UserNotFoundException(message);
        }
        if (taskRepository.existsByAuthor_IdOrExecutor_Id(id, id)) {
            String message = String.format("Status with ID %d is associated with a task, cannot delete.", id);
            log.error(message);
            throw new UserAssociatedWithTaskException(message);
        }
        userRepository.deleteById(id);
        log.info("Successfully deleted user with ID: {}", id);
    }

    @Override
    public long getCurrentUserNameId() {
        String userLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(userLogin)
                .orElseThrow(() -> {
                    String message = String.format("Failed to load user ID. User with email '%s' not found.",
                            userLogin);
                    log.error(message);
                    return new UserNotFoundException(message);
                });
        return currentUser.getId();
    }

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        log.info("Attempting to load User Details by email: {}", email);
        return userRepository.findByEmail(email)
                .map(this::toSpringUser)
                .orElseThrow(() -> {
                    String message = String.format("Failed to load User Details. User with email '%s' not found.",
                            email);
                    log.error(message);
                    return new UserNotFoundException(message);
                });
    }

    private org.springframework.security.core.userdetails.User toSpringUser(final User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("USER")));
    }
}
