package hexlet.code.domain.status;

import java.util.List;

public interface StatusService {
    List<Status> findAll();

    Status findById(Long id);

    Status save(Status newStatus);

    Status updateById(Status updatedStatus, long id);

    void deleteById(long id);
}
