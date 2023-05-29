package hexlet.code.domain.task;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
public interface TaskService {
    List<Task> findAll(Optional<Long> taskStatusId, Optional<Long> executorId,
                       Optional<Long> labelsId, Optional<Long> authorId);

    Task findById(Long id);

    Task save(Task newTask);

    Task configureAndSaveTask(Task newTask, Optional<Long> taskStatusId,
                              Optional<Long> executorId, Optional<Set<Long>> labelIds);

    Task updateTask(Task taskToUpdate, long taskStatusId, long executorId, Set<Long> labelIds, long id);

    Task updateById(Task updatedTask, long id);

    void deleteById(long id);
}
