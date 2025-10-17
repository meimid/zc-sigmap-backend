package com.sigmap.common.societe.mappers;


import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.request.SocieteRequest;
import com.sigmap.common.societe.response.SocieteResponse;
import com.sigmap.common.societe.response.SocieteResponseDto;
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
public interface SocieteMapper {
    SocieteMapper INSTANCE = Mappers.getMapper(SocieteMapper.class);

    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    SocieteResponse map(Societe source);
    Societe map(SocieteRequest source);

    @Mapping(target = "metadata.createdAt", ignore = true)
    @Mapping(target = "metadata.createdBy", ignore = true)
    @Mapping(target = "metadata.updatedAt", ignore = true)
    @Mapping(target = "metadata.updatedBy", ignore = true)
    @Mapping(target = "metadata.deletedBy", ignore = true)
    @Mapping(target = "metadata.isDeleted", ignore = true)
    void updateFromRequest(SocieteRequest source, @MappingTarget Societe target);

    SocieteResponseDto toResponseDto(Societe source);
}
