package com.sigmap.common.direction.mappers;

import com.sigmap.common.direction.Direction;
import com.sigmap.common.direction.request.DirectionRequest;
import com.sigmap.common.direction.response.DirectionResponse;
import com.sigmap.common.direction.response.DirectionResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DirectionMapper {
    DirectionMapper INSTANCE = Mappers.getMapper(DirectionMapper.class);
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    DirectionResponse map(Direction direction);
    DirectionResponseDto mapToDto(Direction direction);
    Direction mapFromRequest(DirectionRequest request);
    void updateFromRequest(DirectionRequest source, @MappingTarget Direction target);


}
