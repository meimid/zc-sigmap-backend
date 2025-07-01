package com.sigmap.common.dac.mapper;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.response.DacResponce;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DacMapper {

    DacMapper INSTANCE = Mappers.getMapper(DacMapper.class);

    @Mapping(target = "ppm", source = "ppm")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "realisation", source = "realisation")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    DacResponce map(Dac source);
}
