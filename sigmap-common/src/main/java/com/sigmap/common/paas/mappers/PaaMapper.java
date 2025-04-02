package com.sigmap.common.paas.mappers;

import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.request.PaaRequest;
import com.sigmap.common.paas.response.PaaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaaMapper {
    PaaMapper INSTANCE = Mappers.getMapper(PaaMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "annee", source = "annee")
//    @Mapping(target = "ac", source = "ac") // Assumes Ac is mapped directly
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "statusId", source = "statusId")
    @Mapping(target = "realisations", source = "realisations") // Maps the full list
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    PaaResponse map(Paa source);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "annee", source = "annee")
//    @Mapping(target = "ac", source = "ac") // Assumes Ac is provided in PaaRequest
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "statusId", source = "statusId")
    @Mapping(target = "realisations", ignore = true) // Ignored as it’s typically set separately
    Paa toEntity(PaaRequest paaRequest);
}
