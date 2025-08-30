package com.sigmap.common.process_verbal_reunion.mapper;

import com.sigmap.common.Participant.Participant;
import com.sigmap.common.Participant.response.ParticipantResponseDto;
import com.sigmap.common.process_verbal_reunion.ProcessVerbalReunion;
import com.sigmap.common.process_verbal_reunion.request.ProcessVerbalReunionRequest;
import com.sigmap.common.process_verbal_reunion.response.ProcessVerbalReunionResponse;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunion.mapper.ReunionMapper;
import com.sigmap.common.reunion.request.ReunionRequest;
import com.sigmap.common.reunion.response.ReunionResponse;
import com.sigmap.common.reunion.response.ReunionResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")

public interface ProcessVerbalReunionMapper {
    ProcessVerbalReunionMapper INSTANCE = Mappers.getMapper(ProcessVerbalReunionMapper.class);
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    ProcessVerbalReunionResponse map(ProcessVerbalReunion source);
    ProcessVerbalReunion map(ProcessVerbalReunionRequest source);
    @Mapping(target = "id", ignore = true)
    void updateFromRequest(ProcessVerbalReunionRequest request, @MappingTarget ProcessVerbalReunion source);

}
