package hexlet.code.domain.task;

import hexlet.code.domain.label.Label;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public final class TaskSpecifications {
    private TaskSpecifications() {
    }

    public static Specification<Task> hasTaskStatus(Long id) {
        return (root, query, cb) -> cb.equal(root.get("taskStatus").get("id"), id);
    }

    public static Specification<Task> hasExecutor(Long id) {
        return (root, query, cb) -> cb.equal(root.get("executor").get("id"), id);
    }

    public static Specification<Task> hasAuthor(Long id) {
        return (root, query, cb) -> cb.equal(root.get("author").get("id"), id);
    }

    public static Specification<Task> hasLabel(Long id) {
        return (root, query, cb) -> {
            Join<Task, Label> labels = root.join("labels");
            return cb.equal(labels.get("id"), id);
        };
    }
}

