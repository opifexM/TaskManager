package hexlet.code.domain.user;

import hexlet.code.domain.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    // private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.forId(id));
    }

    @Override
    public User save(User newUser) {
        newUser.setCreatedAt(LocalDateTime.now());
        // newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        newUser.setPassword(newUser.getPassword());
        return userRepository.save(newUser);
    }

    @Override
    public User updateById(User updatedUser, long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setFirstName(updatedUser.getFirstName());
                    user.setLastName(updatedUser.getLastName());
                    user.setEmail(updatedUser.getEmail());
                    // user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                    user.setPassword(updatedUser.getPassword());
                    return userRepository.save(user);
                })
                .orElseThrow(() -> UserNotFoundException.forId(id));
    }

    @Override
    public void deleteById(long id) {
        if (!userRepository.existsById(id)) {
            throw UserNotFoundException.forId(id);
        }
        userRepository.deleteById(id);
    }
}
