package com.sigmap.common.paas.mappers;

import com.sigmap.common.financement.Financement;
import com.sigmap.common.financement.response.FinancementResponse;
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
    @Mapping(target = "modelAlfrescoId", source = "modelAlfrescoId")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "processInstanceId", source = "processInstanceId")
    @Mapping(target = "realisations", expression = "java(mapRealisations(paa.getRealisations()))")
    @Mapping(target = "docs", expression = "java(mapDocs(paa.getDocs()))")
    PaaResponseWithRealisations mapDetailed(Paa paa);

    /**
     * Maps a Realisation entity to a RealisationResponseDtoForPaa
     */
//    @Mapping(target = "metadata.createdAt", source = "createdAt")
//    @Mapping(target = "metadata.createdBy", source = "createdBy")
//    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
//    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
//    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
//    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
//    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "lots", expression = "java(mapLots(realisation.getLots()))")
    @Mapping(target = "docs", expression = "java(mapDocs(realisation.getDocs()))")
    @Mapping(target = "financements", expression = "java(mapFinancements(realisation.getFinancements()))")
    RealisationResponseDtoForPaa mapRealisation(Realisation realisation);

    @Mapping(target = "financementId", source = "id.financementId")
    @Mapping(target = "realisationId", source = "id.realisation.id")
    @Mapping(target = "realisationVer", source = "id.realisation.version")
    @Mapping(target = "monnaieId", source = "monnaie.id")
    @Mapping(target = "sourceFinancement", source = "sourceFinancement")
    @Mapping(target = "budgetEstimer", source = "budgetEstimer")
    FinancementResponse mapFinancement(Financement financement);

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
     * Helper method to map a list of Financement entities to FinancementResponse DTOs - ADD THIS METHOD
     */
    default List<FinancementResponse> mapFinancements(List<Financement> financements) {
        if (financements == null) {
            return null;
        }
        return financements.stream()
                .filter(financement -> !Boolean.TRUE.equals(financement.getIsDeleted())) // Filter out deleted financements
                .map(this::mapFinancement)
                .collect(Collectors.toList());
    }


    /**
     * Helper method to map a set of Lot entities to LotPaaResponse DTOs
     */
    default List<LotPaaResponse> mapLots(List<Lot> lots) {
        if (lots == null) {
            return null;
        }
        return lots.stream()
                .map(this::mapLot)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map a list of Docs entities to DocsResponseDTO DTOs
     */
    default List<com.sigmap.common.docs.Response.DocsResponseDTO> mapDocs(List<com.sigmap.common.docs.Docs> docs) {
        if (docs == null) {
            return null;
        }
        return docs.stream()
                .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                .map(doc -> {
                    com.sigmap.common.docs.Response.DocsResponseDTO dto = new com.sigmap.common.docs.Response.DocsResponseDTO();
                    dto.setId(doc.getId());
                    dto.setName(doc.getName());
                    dto.setAlfresco_id(doc.getAlfresco_id());
                    dto.setPathFolder(doc.getPathFolder());
                    dto.setFileSize(doc.getFileSize());
                    dto.setContentType(doc.getContentType());
                    dto.setFileExtension(doc.getFileExtension());
                    dto.setAlfrescoName(doc.getAlfrescoName());
                    return dto;
                })
                .collect(Collectors.toList());
    }
} 