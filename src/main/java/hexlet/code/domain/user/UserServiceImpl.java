package hexlet.code.domain.user;

import hexlet.code.domain.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(final UserRepository userRepository,
                           final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
                    String message = String.format("Failed to retrieve user. User with id %d not found.", id);
                    log.error(message);
                    return new UserNotFoundException(message);
                });
    }

    @Override
    public User save(final User newUser) {
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        log.info("Saving new user: {}", newUser);
        User savedUser = userRepository.save(newUser);
        savedUser.setPassword(null);
        log.info("Successfully saved new user: {}", savedUser);
        return savedUser;
    }

    @Override
    public User updateById(final User updatedUser, final long id) {
        log.info("Updating user with ID: {} with data: {}", id, updatedUser);
        User savedUser = userRepository.findById(id)
                .map(user -> {
                    user.setFirstName(updatedUser.getFirstName());
                    user.setLastName(updatedUser.getLastName());
                    user.setEmail(updatedUser.getEmail());
                    user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                    return userRepository.save(user);
                })
                .orElseThrow(() -> {
                    String message = String.format("Failed to update user. User with id %d not found.", id);
                    log.error(message);
                    return new UserNotFoundException(message);
                });
        savedUser.setPassword(null);
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
        userRepository.deleteById(id);
        log.info("Successfully deleted user with ID: {}", id);
    }

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        log.info("Attempting to load user by email: {}", email);
        return userRepository.findByEmail(email)
                .map(this::toSpringUser)
                .orElseThrow(() -> {
                    String message = String.format("Failed to load user. User with email %s not found.", email);
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
