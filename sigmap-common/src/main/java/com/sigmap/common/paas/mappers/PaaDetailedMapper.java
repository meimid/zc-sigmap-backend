package com.sigmap.common.paas.mappers;

import com.sigmap.common.lots.Lot;
import com.sigmap.common.lots.response.LotPaaResponse;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.response.PaaResponseWithRealisations;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.response.RealisationResponseDtoForPaa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting between entity objects and specialized DTOs for PAA with Realisations.
 */
@Mapper(componentModel = "spring")
public interface PaaDetailedMapper {
    PaaDetailedMapper INSTANCE = Mappers.getMapper(PaaDetailedMapper.class);

    /**
     * Maps a Paa entity to a PaaResponseWithRealisations DTO
     */
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "realisations", expression = "java(mapRealisations(paa.getRealisations()))")
    PaaResponseWithRealisations mapDetailed(Paa paa);

    /**
     * Maps a Realisation entity to a RealisationResponseDtoForPaa
     */
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "lots", expression = "java(mapLots(realisation.getLots()))")
    RealisationResponseDtoForPaa mapRealisation(Realisation realisation);

    /**
     * Maps a Lot entity to a LotPaaResponse
     */
    LotPaaResponse mapLot(Lot lot);

    /**
     * Helper method to map a list of Realisation entities to RealisationResponseDtoForPaa DTOs
     */
    default List<RealisationResponseDtoForPaa> mapRealisations(List<Realisation> realisations) {
        if (realisations == null) {
            return null;
        }
        return realisations.stream()
                .map(this::mapRealisation)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map a set of Lot entities to LotPaaResponse DTOs
     */
    default List<LotPaaResponse> mapLots(Set<Lot> lots) {
        if (lots == null) {
            return null;
        }
        return lots.stream()
                .map(this::mapLot)
                .collect(Collectors.toList());
    }
} 