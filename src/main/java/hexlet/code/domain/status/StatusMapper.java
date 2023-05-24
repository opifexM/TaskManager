package hexlet.code.domain.status;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface StatusMapper {
    Status toEntity(StatusDto statusDto);

    StatusDto toDto(Status status);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Status partialUpdate(StatusDto statusDto, @MappingTarget Status status);

    Status toEntity(StatusCreationDto statusCreationDto);

    StatusCreationDto toCreationDto(Status status);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Status partialUpdate(StatusCreationDto statusCreationDto, @MappingTarget Status status);

    Status toEntity(StatusChangingDto statusChangingDto);

    StatusChangingDto toChangingDto(Status status);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Status partialUpdate(StatusChangingDto statusChangingDto, @MappingTarget Status status);
}