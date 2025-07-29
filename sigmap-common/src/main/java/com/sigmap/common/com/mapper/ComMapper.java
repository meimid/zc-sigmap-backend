package com.sigmap.common.com.mapper;

import com.sigmap.common.com.Com;
import com.sigmap.common.com.request.ComRequest;
import com.sigmap.common.com.response.ComResponse;
import com.sigmap.common.com.response.ComResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;


@Mapper
public interface ComMapper {
    ComMapper INSTANCE = Mappers.getMapper(ComMapper.class);
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    ComResponse map(Com source);
    Com map(ComRequest source);
    void updateFromRequest(ComRequest source, @MappingTarget Com target);
    ComResponseDto mapToDto(Com source);
}
