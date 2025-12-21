package com.sigmap.common.appel_offre.mapper;

import com.sigmap.common.appel_offre.AppelOffre;
import com.sigmap.common.appel_offre.response.AppelOffreResponse;
import com.sigmap.common.appel_offre.response.AppelOffreResponseById;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AppelOffreMapper {

    AppelOffreMapper INSTANCE = Mappers.getMapper(AppelOffreMapper.class);

    @Mapping(target = "ppm", source = "ppm")
    @Mapping(target = "realisation", source = "realisation")
    @Mapping(target = "modePassation", source = "realisation.modePassation.type")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    AppelOffreResponse map(AppelOffre source);


}
