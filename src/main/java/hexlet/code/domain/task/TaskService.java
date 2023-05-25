package hexlet.code.domain.task;

import java.util.List;
import java.util.Set;

public interface TaskService {
    List<Task> findAll();

    Task findById(Long id);

    Task save(Task newTask);

    Task createTask(Task newTask, long taskStatusId, long authorId, long executorId, Set<Long> labelIds);

    Task updateTask(Task taskToUpdate, long taskStatusId, long authorId, long executorId, Set<Long> labelIds, long id);

    Task updateById(Task updatedTask, long id);

    void deleteById(long id);
}
