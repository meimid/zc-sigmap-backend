package com.sigmap.common.agpm.mappers;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.response.AgpmResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper interface for converting Agpm entities to AgpmResponse DTOs.
 * Contains only the map function, matching the structure of ComMapper.
 */
@Mapper
public interface AgpmMapper {
    AgpmMapper INSTANCE = Mappers.getMapper(AgpmMapper.class);

    /**
     * Maps an Agpm entity to an AgpmResponse DTO.
     * Matches the exact structure of ComMapper's map function, mapping only metadata fields.
     *
     * @param source The Agpm entity to map
     * @return AgpmResponse DTO
     */
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    AgpmResponse map(Agpm source);
}