package hexlet.code.domain.status;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@SuppressWarnings("unused")
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface StatusMapper {
    Status toEntity(StatusDto statusDto);

    Status toEntity(StatusOperationDto statusOperationDto);

    StatusDto toDto(Status status);

    StatusOperationDto toCreationDto(Status status);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Status partialUpdate(StatusDto statusDto, @MappingTarget Status status);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Status partialUpdate(StatusOperationDto statusOperationDto, @MappingTarget Status status);
}
