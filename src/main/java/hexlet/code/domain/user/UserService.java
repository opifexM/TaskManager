package hexlet.code.domain.user;

import java.util.List;

public interface UserService {
    List<User> findAll();

    User findById(Long id);

    User save(User newUser);

    User updateById(User updatedUser, long id);

    void deleteById(long id);

    long getCurrentUserNameId();
}
