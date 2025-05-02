package com.sigmap.common.ppms.mappers;

import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.request.PpmRequest;
import com.sigmap.common.ppms.response.PpmResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PpmMapper {
    PpmMapper INSTANCE = Mappers.getMapper(PpmMapper.class);

    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "status", source = "status")
    PpmResponse map(Ppm source);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version", defaultValue = "1")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "annee", source = "annee")
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "status", source = "status")
    Ppm toEntity(PpmRequest request);
}
