package hexlet.code.domain.task;

import java.util.List;

public interface TaskService {
    List<Task> findAll();

    Task findById(Long id);

    Task save(Task newTask);

    Task createTask(Task newTask, long taskStatusId, long authorId, long executorId);

    Task updateTask(Task taskToUpdate, long taskStatusId, long authorId, long executorId, long id);

    Task updateById(Task updatedTask, long id);

    void deleteById(long id);
}
