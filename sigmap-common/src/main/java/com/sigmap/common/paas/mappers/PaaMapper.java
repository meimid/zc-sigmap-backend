package com.sigmap.common.paas.mappers;

import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.request.PaaRequest;
import com.sigmap.common.paas.response.PaaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaaMapper {
    PaaMapper INSTANCE = Mappers.getMapper(PaaMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version", defaultValue = "1")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "annee", source = "annee")
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "realisations", source = "realisations") // Maps the full list
    @Mapping(target = "processInstanceId", source = "processInstanceId")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    PaaResponse map(Paa source);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version", defaultValue = "1")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "annee", ignore = true)
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "realisations", ignore = true) // Ignored as it's typically set separately
    Paa toEntity(PaaRequest paaRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "annee", source = "annee")
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "realisations", ignore = true) // Ignored as it's typically managed separately
    Paa updatePaaFromRequest(PaaRequest request, @MappingTarget Paa paa);
}
