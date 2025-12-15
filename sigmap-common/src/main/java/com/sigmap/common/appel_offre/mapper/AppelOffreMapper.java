package com.sigmap.common.appel_offre.mapper;

import com.sigmap.common.appel_offre.AppelOffre;
import com.sigmap.common.appel_offre.request.AppelOffreEditRequest;
import com.sigmap.common.appel_offre.request.CreateAppelOffreRequest;
import com.sigmap.common.appel_offre.response.AppelOffreResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
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

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ppm", ignore = true) // Handled in Service
    @Mapping(target = "realisation", ignore = true) // Handled in Service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    AppelOffre createRequestToEntity(CreateAppelOffreRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ppm", ignore = true) // Handled in Service if updated
    @Mapping(target = "realisation", ignore = true) // Handled in Service if updated
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntityFromEditRequest(AppelOffreEditRequest request, @MappingTarget AppelOffre entity);
}
