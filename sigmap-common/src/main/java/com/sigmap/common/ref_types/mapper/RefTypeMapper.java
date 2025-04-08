package com.sigmap.common.ref_types.mapper;

import com.sigmap.common.ref_types.RefType;
import com.sigmap.common.ref_types.request.RefTypeRequest;
import com.sigmap.common.ref_types.response.RefTypeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RefTypeMapper {
    RefTypeMapper INSTANCE = Mappers.getMapper(RefTypeMapper.class);
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    RefTypeResponse map(RefType source);
    RefType map(RefTypeRequest source);
}
