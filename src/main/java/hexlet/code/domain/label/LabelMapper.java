package hexlet.code.domain.label;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface LabelMapper {
    Label toEntity(LabelDto labelDto);

    Label toEntity(LabelChangingDto labelChangingDto);

    Label toEntity(LabelCreationDto labelCreationDto);

    LabelDto toDto(Label label);

    LabelCreationDto toCreationDto(Label label);

    LabelChangingDto toChangingDto(Label label);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Label partialUpdate(LabelDto labelDto, @MappingTarget Label label);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Label partialUpdate(LabelCreationDto labelCreationDto, @MappingTarget Label label);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Label partialUpdate(LabelChangingDto labelChangingDto, @MappingTarget Label label);
}
