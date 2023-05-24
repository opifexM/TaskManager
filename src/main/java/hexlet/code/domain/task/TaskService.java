package hexlet.code.domain.task;

import java.util.List;

public interface TaskService {
    List<Task> findAll();

    Task findById(Long id);

    Task save(Task newTask);

    Task updateById(Task updatedTask, long id);

    void deleteById(long id);
}
