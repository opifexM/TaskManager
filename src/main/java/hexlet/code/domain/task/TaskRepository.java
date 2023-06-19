package hexlet.code.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    Optional<Task> findByName(String name);

    boolean existsByLabels_Id(Long id);

    boolean existsByTaskStatus_Id(Long id);

    boolean existsByAuthor_IdOrExecutor_Id(Long id1, Long id2);
}
