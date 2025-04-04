package com.sigmap.common.ppms.mappers;

import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.response.PpmResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PpmMapper {
    PpmMapper INSTANCE = Mappers.getMapper(PpmMapper.class);

    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    PpmResponse map(Ppm source);
}
