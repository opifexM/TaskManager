package hexlet.code.domain.task;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@SuppressWarnings("unused")
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {
    Task toEntity(TaskDto taskDto);

    @Mapping(target = "taskStatus", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "executor", ignore = true)
    @Mapping(target = "labels", ignore = true)
    Task toEntity(TaskOperationDto taskOperationDto);

    TaskDto toDto(Task task);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Task partialUpdate(TaskDto taskDto, @MappingTarget Task task);
}
