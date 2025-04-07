package com.sigmap.common.agpm.mappers;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.agpm.response.AgpmResponse;
import com.sigmap.common.docs.Docs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

/**
 * Mapper interface for converting between Agpm entities, requests, and responses.
 */
@Mapper(componentModel = "spring")
public interface AgpmMapper {
    AgpmMapper INSTANCE = Mappers.getMapper(AgpmMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "contenu", source = "contenu")
    @Mapping(target = "statusId", source = "statusId")
    @Mapping(target = "ac", source = "ac")
    @Mapping(target = "docs", source = "docs")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    AgpmResponse map(Agpm source);

    List<AgpmResponse> mapToResponseList(List<Agpm> agpms);

    default Page<AgpmResponse> mapToResponse(Page<Agpm> agpmPage) {
        List<AgpmResponse> responses = mapToResponseList(agpmPage.getContent());
        return new PageImpl<>(responses, agpmPage.getPageable(), agpmPage.getTotalElements());
    }

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "contenu", source = "contenu")
    @Mapping(target = "statusId", source = "statusId")
    @Mapping(target = "ac", ignore = true)
    @Mapping(target = "docs", ignore = true)
    Agpm toEntity(AgpmRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "contenu", source = "contenu")
    @Mapping(target = "statusId", source = "statusId")
    @Mapping(target = "ac", ignore = true)
    @Mapping(target = "docs", ignore = true)
    void updateAgpmFromRequest(AgpmRequest request, @MappingTarget Agpm agpm);
}