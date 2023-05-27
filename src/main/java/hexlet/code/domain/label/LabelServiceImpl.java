package hexlet.code.domain.label;

import hexlet.code.domain.task.TaskRepository;
import hexlet.code.exception.DuplicateLabelException;
import hexlet.code.exception.LabelAssociatedWithTaskException;
import hexlet.code.exception.LabelNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;
    private final TaskRepository taskRepository;

    @Override
    public List<Label> findAll() {
        log.info("Retrieving all labels");
        return labelRepository.findAll();
    }

    @Override
    public Label findById(Long id) {
        log.info("Retrieving label with ID: {}", id);
        return labelRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Failed to retrieve label. Label with ID %d not found.", id);
                    log.error(message);
                    return new LabelNotFoundException(message);
                });
    }

    @Override
    public Label save(Label newLabel) {
        log.info("Saving new label: {}", newLabel);
        if (labelRepository.findByName(newLabel.getName()).isPresent()) {
            String message = String.format("Failed to save label. Label with name '%s' already exists.",
                    newLabel.getName());
            log.error(message);
            throw new DuplicateLabelException(message);
        }
        Label savedLabel = labelRepository.save(newLabel);
        log.info("Successfully saved new label: {}", savedLabel);
        return savedLabel;
    }

    @Override
    public Label updateById(Label updatedLabel, long id) {
        log.info("Updating label with ID: {} with data: {}", id, updatedLabel);
        Label savedLabel = labelRepository.findById(id)
                .map(label -> {
                    label.setName(updatedLabel.getName());
                    return labelRepository.save(label);
                })
                .orElseThrow(() -> {
                    String message = String.format("Failed to update label. Label with ID %d not found.", id);
                    log.error(message);
                    return new LabelNotFoundException(message);
                });
        log.info("Successfully updated label {}", savedLabel);
        return savedLabel;
    }

    @Override
    public void deleteById(long id) {
        log.info("Deleting label with ID: {}", id);
        if (!labelRepository.existsById(id)) {
            String message = String.format("Failed to delete label. Label with ID %d not found.", id);
            log.error(message);
            throw new LabelNotFoundException(message);
        }
        if (taskRepository.existsByLabels_Id(id)) {
            String message = String.format("Label with ID %d is associated with a task, cannot delete.", id);
            log.error(message);
            throw new LabelAssociatedWithTaskException(message);
        }
        labelRepository.deleteById(id);
        log.info("Successfully deleted label with ID: {}", id);
    }
}
