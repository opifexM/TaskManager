package hexlet.code.domain.task;

import java.util.List;

public interface TaskService {
    List<Task> findAll();

    Task findById(Long id);

    Task save(Task newTask);

    Task createTask(TaskCreationDto taskCreationDto);

    Task updateById(Task updatedTask, long id);

    void deleteById(long id);

    Task updateTask(TaskChangingDto taskChangingDto, long id);
}
