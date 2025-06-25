package com.sigmap.common.pv_membre_present.mapper;

import com.sigmap.common.pv_membre_present.PvMembrePresent;
import com.sigmap.common.pv_membre_present.response.PvMembrePresentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PvMembrePresentMapper {
    PvMembrePresentMapper INSTANCE = Mappers.getMapper(PvMembrePresentMapper.class);

/*    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    PvMembrePresentResponseDto toDo(PvMembrePresent sourse);*/
}
