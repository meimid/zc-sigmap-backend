package com.sigmap.common.paas.mappers;

import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.response.PaaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaaMapper {
    PaaMapper INSTANCE = Mappers.getMapper(PaaMapper.class);

    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    PaaResponse map(Paa source);
}
