package hexlet.code.domain.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserSecurityService {

    private final UserService userService;

    @Autowired
    public UserSecurityService(UserService userService) {
        this.userService = userService;
    }

    public boolean isOwner(Long id) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findById(id);
        return user.getEmail().equals(currentUser);
    }
}
