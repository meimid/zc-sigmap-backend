package com.sigmap.common.activite.mapper;

import com.sigmap.common.activite.Activite;
import com.sigmap.common.activite.request.ActiviteRequest;
import com.sigmap.common.activite.response.ActiviteResponse;
import com.sigmap.common.activite.response.ActiviteResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")

public interface ActiviteMapper {
    ActiviteMapper INSTANCE = Mappers.getMapper(ActiviteMapper.class);

    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    ActiviteResponse map(Activite activite);
    ActiviteResponseDto mapToDto(Activite activite) ;
    Activite mapFromRequest(ActiviteRequest request);
}
