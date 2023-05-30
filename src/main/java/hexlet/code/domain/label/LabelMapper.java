package hexlet.code.domain.label;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@SuppressWarnings("unused")
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface LabelMapper {
    Label toEntity(LabelDto labelDto);

    Label toEntity(LabelOperationDto labelOperationDto);

    LabelDto toDto(Label label);

    LabelOperationDto toCreationDto(Label label);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Label partialUpdate(LabelDto labelDto, @MappingTarget Label label);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Label partialUpdate(LabelOperationDto labelOperationDto, @MappingTarget Label label);
}
