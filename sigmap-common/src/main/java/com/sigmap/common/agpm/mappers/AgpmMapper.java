package com.sigmap.common.agpm.mappers;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.response.AgpmResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Use MapStruct to generate code for mapping between Objects.
 *
 * @see <a href="https://mapstruct.org/documentation/stable/reference/html/">MapStruct Documentation</a>
 */

@Mapper
public interface AgpmMapper {
    AgpmMapper INSTANCE = Mappers.getMapper(AgpmMapper.class);

//    @Mapping(target = "actId", source = "ac.id")
//    @Mapping(target = "docIds", expression = "java(agpm.getDocs().stream().map(Doc::getId).collect(java.util.stream.Collectors.toSet()))")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    AgpmResponse map(Agpm agpm);
}