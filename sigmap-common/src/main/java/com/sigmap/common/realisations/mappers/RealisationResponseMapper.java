package com.sigmap.common.realisations.mappers;

import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.response.RealisationResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RealisationResponseMapper {
    RealisationResponseMapper INSTANCE = Mappers.getMapper(RealisationResponseMapper.class);

    @Mapping(target = "lots", ignore = true)
    RealisationResponseDTO mapWithoutLots(Realisation realisation);

    RealisationResponseDTO map(Realisation realisation);
} 