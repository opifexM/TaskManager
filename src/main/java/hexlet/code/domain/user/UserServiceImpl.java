package hexlet.code.domain.user;

import hexlet.code.domain.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
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
                .orElseThrow(() -> UserNotFoundException.forId(id));
    }

    @Override
    public User save(final User newUser) {
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        log.info("Saving new user: {}", newUser);
        return userRepository.save(newUser);
    }

    @Override
    public User updateById(final User updatedUser, final long id) {
        log.info("Updating user with ID: {} with data: {}", id, updatedUser);
        return userRepository.findById(id)
                .map(user -> {
                    user.setFirstName(updatedUser.getFirstName());
                    user.setLastName(updatedUser.getLastName());
                    user.setEmail(updatedUser.getEmail());
                    user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                    return userRepository.save(user);
                })
                .orElseThrow(() -> UserNotFoundException.forId(id));
    }

    @Override
    public void deleteById(final long id) {
        log.info("Deleting user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            throw UserNotFoundException.forId(id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        log.info("Attempting to load user by email: {}", email);
        return userRepository.findByEmail(email)
                .map(this::toSpringUser)
                .orElseThrow(() -> {
                    log.error("User not found for email: {}", email);
                    return new UsernameNotFoundException(email);
                });
    }

    private org.springframework.security.core.userdetails.User toSpringUser(final User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("USER")));
    }
}
