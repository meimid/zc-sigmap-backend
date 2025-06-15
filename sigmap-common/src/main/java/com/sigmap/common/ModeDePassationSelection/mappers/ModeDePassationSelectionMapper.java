package com.sigmap.common.ModeDePassationSelection.mappers;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.ModeDePassationSelection.response.ModeDePassationSelectionResponse;
import com.sigmap.common.ModeDePassationSelection.response.ModeDePassationSelectionResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ModeDePassationSelectionMapper {
    ModeDePassationSelectionMapper INSTANCE = Mappers.getMapper(ModeDePassationSelectionMapper.class);

    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    ModeDePassationSelectionResponse map(ModeDePassationSelection modeDePassaionSelection);
    ModeDePassationSelectionResponseDto mapToDto(ModeDePassationSelection modeDePassaionSelection) ;
}
