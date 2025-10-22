package com.sigmap.common.adresse.mappers;

import com.sigmap.common.adresse.Adresse;
import com.sigmap.common.adresse.request.AdresseRequest;
import com.sigmap.common.adresse.response.AdresseResponse;
import com.sigmap.common.adresse.response.AdresseResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AdresseMapper {
    AdresseMapper INSTANCE = Mappers.getMapper(AdresseMapper.class);
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    AdresseResponse map(Adresse source);
    Adresse map(AdresseRequest source);
    AdresseResponseDto toDto(Adresse source);
    void updateFromRequest(AdresseRequest source, @MappingTarget Adresse target);


}
