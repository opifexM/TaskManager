package hexlet.code.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@SuppressWarnings("unused")
@Service
@RequiredArgsConstructor
public class UserSecurityService {

    private final UserService userService;

    public boolean isOwner(Long id) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findById(id);
        return user.getEmail().equals(currentUser);
    }
}
