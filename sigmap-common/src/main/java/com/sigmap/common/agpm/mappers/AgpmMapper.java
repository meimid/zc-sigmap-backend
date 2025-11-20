package com.sigmap.common.agpm.mappers;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.agpm.response.*;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.ref_params.RefParam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    @Mapping(target = "annee", source = "annee")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "ac", source = "ac")
    @Mapping(target = "docs", source = "docs")
    @Mapping(target = "modelAlfrescoId", source = "modelAlfrescoId")
    @Mapping(target = "processInstanceId", source = "processInstanceId")
    @Mapping(target = "actualise", source = "actualise")
    @Mapping(target = "nbrActualisation", source = "nbrActualisation")
    @Mapping(target = "etapeProcess", source = "etapeProcess")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    AgpmResponse map(Agpm source);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "contenu", source = "contenu")
    @Mapping(target = "annee", source = "annee")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "ac", ignore = true)
    @Mapping(target = "docs", ignore = true)
    @Mapping(target = "actualise", ignore = true)
    @Mapping(target = "nbrActualisation", ignore = true)
    Agpm toEntity(AgpmRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "contenu", source = "contenu")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "annee", source = "annee")
    @Mapping(target = "processInstanceId", source = "processInstanceId")
    @Mapping(target = "ac", source = "ac")
    @Mapping(target = "docs", source = "docs")
    @Mapping(target = "modelAlfrescoId", source = "modelAlfrescoId")
    @Mapping(target = "actualise", source = "actualise")
    @Mapping(target = "nbrActualisation", source = "nbrActualisation")
    @Mapping(target = "etapeProcess", source = "etapeProcess")
    AgpmResponseDto mapToDto(Agpm source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "contenu", source = "contenu")
    @Mapping(target = "annee", source = "annee")
    @Mapping(target = "ac", ignore = true)
    @Mapping(target = "docs", ignore = true)
    @Mapping(target = "actualise", ignore = true)
    @Mapping(target = "nbrActualisation", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateAgpmFromRequest(AgpmRequest request, @MappingTarget Agpm agpm);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "modelAlfrescoId", source = "modelAlfrescoId")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "actualise", source = "actualise")
    @Mapping(target = "nbrActualisation", source = "nbrActualisation")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    AgpmMinimalResponseDto toMinimalResponseDto(Agpm agpm);

    AgpmResponseDtoSoft toDto(Agpm agpm);
}
