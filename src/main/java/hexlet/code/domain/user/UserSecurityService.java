package hexlet.code.domain.user;

import hexlet.code.domain.task.Task;
import hexlet.code.domain.task.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@SuppressWarnings("unused")
@Service
@RequiredArgsConstructor
public class UserSecurityService {

    private final UserService userService;
    private final TaskService taskService;

    public boolean isOwner(Long userIdRequest) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findById(userIdRequest);
        return user.getEmail().equals(currentUser);
    }

    public boolean isTaskOwner(Long taskIdRequest) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        Task task = taskService.findById(taskIdRequest);
        User author = task.getAuthor();
        return author.getEmail().equals(currentUser);
    }
}
