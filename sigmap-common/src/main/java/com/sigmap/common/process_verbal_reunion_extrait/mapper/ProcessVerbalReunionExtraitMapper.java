package com.sigmap.common.process_verbal_reunion_extrait.mapper;

import com.sigmap.common.process_verbal_reunion_extrait.ProcessVerbalReunionExtrait;
import com.sigmap.common.process_verbal_reunion_extrait.request.ProcessVerbalReunionExtraitRequest;
import com.sigmap.common.process_verbal_reunion_extrait.response.ProcessVerbalReunionExtraitRespons;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(
        componentModel = "spring"
)
public interface ProcessVerbalReunionExtraitMapper {

    ProcessVerbalReunionExtraitMapper INSTANCE = Mappers.getMapper(ProcessVerbalReunionExtraitMapper.class);

    ProcessVerbalReunionExtraitRespons toDto(ProcessVerbalReunionExtrait extrait);
    ProcessVerbalReunionExtrait toEntity(ProcessVerbalReunionExtraitRequest request);


}