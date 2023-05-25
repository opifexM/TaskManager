package hexlet.code.domain.label;

import java.util.List;

public interface LabelService {
    List<Label> findAll();

    Label findById(Long id);

    Label save(Label newLabel);

    Label updateById(Label updatedLabel, long id);

    void deleteById(long id);
}
