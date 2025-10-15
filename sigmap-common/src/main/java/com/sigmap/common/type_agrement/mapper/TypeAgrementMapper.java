package com.sigmap.common.type_agrement.mapper;


import com.sigmap.common.type_agrement.TypeAgrement;
import com.sigmap.common.type_agrement.request.TypeAgrementRequest;
import com.sigmap.common.type_agrement.response.TypeAgrementResponse;
import com.sigmap.common.type_agrement.response.TypeAgrementResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")

public interface TypeAgrementMapper {

    TypeAgrementMapper INSTANCE = Mappers.getMapper(TypeAgrementMapper.class);

    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    TypeAgrementResponse map(TypeAgrement TypeAgrement);
    TypeAgrementResponseDto mapToDto(TypeAgrement TypeAgrement) ;
    TypeAgrement mapFromRequest(TypeAgrementRequest request);
}
