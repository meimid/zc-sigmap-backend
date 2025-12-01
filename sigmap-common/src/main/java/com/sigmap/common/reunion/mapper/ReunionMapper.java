package com.sigmap.common.reunion.mapper;

import com.sigmap.common.Participant.Participant;

import com.sigmap.common.Participant.mapper.ParticipantMapper;
import com.sigmap.common.Participant.response.ParticipantResponseDto;
import com.sigmap.common.ac.mapper.AcMapper;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunion.request.ReunionRequest;
import com.sigmap.common.reunion.response.*;

import com.sigmap.common.reunionDac.ReunionDac;
import com.sigmap.common.reunionDac.mapper.ReunionDacMapper;
import com.sigmap.common.reunionDac.response.ReunionDacResponse;
import com.sigmap.common.reunionPpm.mapper.ReunionPpmMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;


@Mapper(
        componentModel = "spring"

)
public interface ReunionMapper {
    ReunionMapper INSTANCE = Mappers.getMapper(ReunionMapper.class);
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(source = "com", target = "com")
    @Mapping(source = "reunionPpms", target = "reunionPpms")
    @Mapping(source = "reunionDacs", target = "reunionDacs")
    @Mapping(source = "participants", target = "participants")
    @Mapping(source = "processVerbal",target = "processVerbal")
    ReunionResponse map(Reunion reunion);


    @Mapping(target = "id", ignore = true)
    Reunion map(ReunionRequest request);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "com", ignore = true)
    @Mapping(target = "participants", ignore = true)
    @Mapping(target = "reunionPpms", ignore = true)
    @Mapping(target = "reunionDacs", ignore = true)
    @Mapping(target ="status", ignore = true )
    @Mapping(source = "processVerbal",target = "processVerbal")
    void updateFromRequest(ReunionRequest request, @MappingTarget Reunion reunion);

    ParticipantResponseDto toDto(Participant participant);
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(source = "com.nom", target = "comName")
    ReunionResponseDto mapToDto(Reunion reunion);
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(source = "participants", target = "participants")
    @Mapping(source = "processVerbal",target = "processVerbal")
    ReunionWithParticipantsAndPvDto mapToReunionWithParticipantsAndPvDto(Reunion reunion);
    ReunionDropListDto mapToDropListDto(Reunion reunion);

    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(source = "com.nom", target = "comName")
    ReunionSearchResponseDto mapToReunionSearchResponseDto(Reunion reunion);

}