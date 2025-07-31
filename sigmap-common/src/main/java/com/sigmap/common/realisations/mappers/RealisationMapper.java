package com.sigmap.common.realisations.mappers;

import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.request.RealisationRequest;
import com.sigmap.common.realisations.response.RealisationResponse;
import com.sigmap.common.realisations.response.RealisationResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RealisationMapper {
    RealisationMapper INSTANCE = Mappers.getMapper(RealisationMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version", defaultValue = "1")
    @Mapping(target = "realisationEnvisagees", source = "realisationEnvisagees")
    @Mapping(target = "service", source = "service")
    @Mapping(target = "direction", source = "direction")
    @Mapping(target = "sourceFinancement", source = "sourceFinancement")
    @Mapping(target = "imputationBudgetaire",source  = "imputationBudgetaire")
    @Mapping(target = "monais", source = "monais")
    @Mapping(target = "typeMarcher", source = "typeMarcher")
    @Mapping(target = "modeSelection", source = "modeSelection")
    @Mapping(target = "datePrevLps", source = "datePrevLps")
    @Mapping(target = "datePrevAc", source = "datePrevAc")
    @Mapping(target = "datePrevDp", source = "datePrevDp")
    @Mapping(target = "datePrevAp", source = "datePrevAp")
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "isPublished", source = "isPublished")
    @Mapping(target = "publishedAt", source = "publishedAt")
    @Mapping(target = "sort", source = "sort")
    @Mapping(target = "statut", source = "statut")
    @Mapping(target = "paas", source = "paas")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    RealisationResponse map(Realisation source);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version", defaultValue = "1")
    @Mapping(target = "realisationEnvisagees", source = "realisationEnvisagees")
    @Mapping(target = "service", source = "service")
    @Mapping(target = "direction", source = "direction")
    @Mapping(target = "sourceFinancement", source = "sourceFinancement")
    @Mapping(target = "imputationBudgetaire",source  = "imputationBudgetaire")
    @Mapping(target = "monais", source = "monais")
    @Mapping(target = "typeMarcher", source = "typeMarcher")
    @Mapping(target = "modeSelection", source = "modeSelection")
    @Mapping(target = "datePrevLps", source = "datePrevLps")
    @Mapping(target = "datePrevAc", source = "datePrevAc")
    @Mapping(target = "datePrevDp", source = "datePrevDp")
    @Mapping(target = "datePrevAp", source = "datePrevAp")
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "isPublished", source = "isPublished")
    @Mapping(target = "publishedAt", source = "publishedAt")
    @Mapping(target = "sort", source = "sort")
    @Mapping(target = "statut", source = "statut")
    @Mapping(target = "paas", ignore = true)
    @Mapping(target = "lots", ignore = true)
    @Mapping(target = "ppms", ignore = true)
    @Mapping(target = "docs", ignore = true)
    Realisation toEntity(RealisationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "statut", source = "statut")
    Realisation updateRealisationFromRequest(RealisationRequest request, @MappingTarget Realisation realisation);

    @Mapping(target = "statut", source = "statut")
    RealisationResponseDTO mapWithDTO(Realisation realisation);
}