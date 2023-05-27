package hexlet.code.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
//todo
//public interface TaskRepository extends JpaRepository<Task, Long>, QuerydslPredicateExecutor<Task> {
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    Optional<Task> findByName(String name);

    boolean existsByLabels_Id(Long id);
    @Query("select (count(t) > 0) from Task t inner join t.labels labels where labels.id = ?1")
    boolean existsByLabelsId(Long id);

    boolean existsByTaskStatus_Id(Long id);

    boolean existsByAuthor_IdOrExecutor_Id(Long id1, Long id2);
}