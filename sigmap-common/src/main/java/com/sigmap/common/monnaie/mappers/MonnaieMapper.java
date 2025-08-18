package com.sigmap.common.monnaie.mappers;

import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.monnaie.request.MonnaieRequest;
import com.sigmap.common.monnaie.response.MonnaieResponse;
import com.sigmap.common.monnaie.response.MonnaieResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * Use MapStruct to generate code for mapping between Objects.
 *
 * @see <a href="https://mapstruct.org/documentation/stable/reference/html/">MapStruct Documentation</a>
 */

@Mapper
public interface MonnaieMapper {
    MonnaieMapper INSTANCE = Mappers.getMapper(MonnaieMapper.class);

    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    MonnaieResponse map(Monnaie source);
    Monnaie map(MonnaieRequest source);
    void updateFromRequest(MonnaieRequest source, @MappingTarget Monnaie target);
    MonnaieResponseDto mapToDto(Monnaie source);
}
