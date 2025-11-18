package com.sigmap.common.ppms.mappers;

import com.sigmap.common.financement.Financement;
import com.sigmap.common.financement.response.FinancementResponse;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.request.PpmRequest;
import com.sigmap.common.ppms.response.PpmResponse;
import com.sigmap.common.ppms.response.PpmResponseDto;
import com.sigmap.common.ppms.response.PpmResponseSoftDto;
import com.sigmap.common.ppms.response.PpmResponseSoftDtoWithMetaData;
import com.sigmap.common.realisations.Realisation;
// CHANGE HERE: Import the correct DTO
import com.sigmap.common.realisations.response.RealisationResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PpmMapper {
    PpmMapper INSTANCE = Mappers.getMapper(PpmMapper.class);

    // ... (other mappings remain the same) ...
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version", defaultValue = "1")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "processInstanceId", source = "processInstanceId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "actualise", source = "actualise")
    @Mapping(target = "nbrActualisation", source = "nbrActualisation")
    @Mapping(target = "etapeProcess", source = "etapeProcess")
    PpmResponse map(Ppm source);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version", defaultValue = "1")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "status", source = "status")
    Ppm toEntity(PpmRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "annee", source = "annee")
    @Mapping(target = "ac", source = "ac")
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "docs", source = "docs")
    @Mapping(target = "modelAlfrescoId", source = "modelAlfrescoId")
    @Mapping(target = "remarque", source = "remarque")
    @Mapping(target = "realisations", expression = "java(mapRealisations(source.getRealisations()))")
    @Mapping(target = "processInstanceId", source = "processInstanceId")
    @Mapping(target = "actualise", source = "actualise")
    @Mapping(target = "etapeProcess", source = "etapeProcess")
    @Mapping(target = "nbrActualisation", source = "nbrActualisation")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    PpmResponseDto mapToDto(Ppm source);

    @Mapping(target = "etapeProcess", source = "etapeProcess")
    PpmResponseSoftDto toDto(Ppm ppm);
    @Mapping(target = "actualise", source = "actualise")
    @Mapping(target = "etapeProcess", source = "etapeProcess")
    @Mapping(target = "nbrActualisation", source = "nbrActualisation")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    PpmResponseSoftDtoWithMetaData mapToDtoWithMetaData(Ppm ppm);

    @Mapping(target = "financementId", source = "id.financementId")
    @Mapping(target = "realisationId", source = "id.realisation.id")
    @Mapping(target = "realisationVer", source = "id.realisation.version")
    @Mapping(target = "monnaieId", source = "monnaie.id")
    @Mapping(target = "sourceFinancement", source = "sourceFinancement")
    @Mapping(target = "budgetEstimer", source = "budgetEstimer")
    FinancementResponse mapFinancement(Financement financement);

    // CHANGE HERE: The return type is now RealisationResponseDTO
    @Mapping(target = "financements", expression = "java(mapFinancements(realisation.getFinancements()))")
    RealisationResponseDTO mapRealisation(Realisation realisation);

    /**
     * Helper method to map a list of Realisation entities.
     * CHANGE HERE: The return type is now List<RealisationResponseDTO>
     */
    default List<RealisationResponseDTO> mapRealisations(List<Realisation> realisations) {
        if (realisations == null) {
            return Collections.emptyList();
        }
        return realisations.stream()
                .map(this::mapRealisation)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map a list of Financement entities.
     */
    default List<FinancementResponse> mapFinancements(List<Financement> financements) {
        if (financements == null) {
            return Collections.emptyList();
        }
        return financements.stream()
                .map(this::mapFinancement)
                .collect(Collectors.toList());
    }
}