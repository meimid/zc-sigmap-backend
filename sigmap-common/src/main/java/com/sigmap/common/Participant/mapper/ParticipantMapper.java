package com.sigmap.common.Participant.mapper;

import com.sigmap.common.Participant.Participant;
import com.sigmap.common.Participant.request.ParticipantRequest;
import com.sigmap.common.Participant.response.ParticipantResponse;
import com.sigmap.common.Participant.response.ParticipantResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ParticipantMapper {

    ParticipantMapper INSTANCE = Mappers.getMapper(ParticipantMapper.class);

    /**
     * Maps a Participant entity to a ParticipantResponse DTO, including metadata.
     */
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    ParticipantResponse map(Participant participant);

    /**
     * Maps a ParticipantRequest DTO to a Participant entity.
     */
    Participant map(ParticipantRequest request);

    ParticipantResponseDto toDto(Participant participant);

    /**
     * Updates an existing Participant entity from a ParticipantRequest DTO.
     */
    void updateFromRequest(ParticipantRequest request, @MappingTarget Participant participant);
    List<ParticipantResponseDto> toDtos(List<Participant> participants);
}