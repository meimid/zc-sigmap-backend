package com.sigmap.common.realisations.mappers;

import com.sigmap.common.financement.Financement;
import com.sigmap.common.financement.response.FinancementResponse;
import com.sigmap.common.lots.Lot;
import com.sigmap.common.lots.mappers.LotMapper;
import com.sigmap.common.lots.response.LotPaaResponse;
import com.sigmap.common.lots.response.LotResponse;
import com.sigmap.common.lots.response.LotResponseDTO;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.request.RealisationRequest;
import com.sigmap.common.realisations.response.RealisationResponse;
import com.sigmap.common.realisations.response.RealisationResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.sigmap.common.source_financement.mappers.SourceFinancementMapper;

@Mapper(componentModel = "spring")
public interface RealisationMapper {
    RealisationMapper INSTANCE = Mappers.getMapper(RealisationMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version", defaultValue = "1")
    @Mapping(target = "realisationEnvisagees", source = "realisationEnvisagees")
    @Mapping(target = "beneficiaire", source = "beneficiaire")
    // @Mapping(target = "service", source = "service")
    // @Mapping(target = "direction", source = "direction")
    // @Mapping(target = "sourceFinancement", source = "sourceFinancement")
    @Mapping(target = "imputationBudgetaire", source = "imputationBudgetaire")
    // @Mapping(target = "monnaie", source = "monnaie")
    @Mapping(target = "typeMarcher", source = "typeMarcher")
    @Mapping(target = "modePassation", source = "modePassation")
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
    @Mapping(target = "beneficiaire", source = "beneficiaire")
    // @Mapping(target = "service", source = "service")
    // @Mapping(target = "direction", source = "direction")
    // @Mapping(target = "sourceFinancement", source = "sourceFinancement")
    @Mapping(target = "imputationBudgetaire", source = "imputationBudgetaire")
    // @Mapping(target = "monnaie", source = "monnaie")
    @Mapping(target = "typeMarcher", source = "typeMarcher")
    @Mapping(target = "modePassation", source = "modePassation")
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

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version", defaultValue = "1")
    @Mapping(target = "realisationEnvisagees", source = "realisationEnvisagees")
    @Mapping(target = "financements", expression = "java(mapFinancements(source.getFinancements()))")
    @Mapping(target = "lots", expression = "java(mapLots(source.getLots()))")

    // @Mapping(target = "lots", expression =
    // "java(mapLots(realisation.getLots()))")
    @Mapping(target = "beneficiaire", source = "beneficiaire")
    @Mapping(target = "typeMarcher", source = "typeMarcher")
    @Mapping(target = "modePassation", source = "modePassation")
    @Mapping(target = "datePrevLps", source = "datePrevLps")
    @Mapping(target = "datePrevAc", source = "datePrevAc")
    @Mapping(target = "datePrevDp", source = "datePrevDp")
    @Mapping(target = "datePrevAp", source = "datePrevAp")
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "sort", source = "sort")
    @Mapping(target = "statut", source = "statut")
    RealisationResponseDTO mapForAo(Realisation source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "statut", source = "statut")
    Realisation updateRealisationFromRequest(RealisationRequest request, @MappingTarget Realisation realisation);

    @Mapping(target = "statut", source = "statut")
    RealisationResponseDTO mapWithDTO(Realisation realisation);

    @Mapping(target = "financementId", source = "id.financementId")
    @Mapping(target = "realisationId", source = "id.realisationId")
    @Mapping(target = "realisationVer", source = "id.realisationVer")
    @Mapping(target = "monnaieId", source = "monnaie.id")
    @Mapping(target = "sourceFinancement", source = "sourceFinancement")
    @Mapping(target = "budgetEstimer", source = "budgetEstimer")
    FinancementResponse mapFinancement(Financement financement);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "budget", source = "budget")
    LotResponseDTO mapLot(Lot lot);

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

    /**
     * Helper method to map a list of Lots entities.
     */
    default List<LotResponseDTO> mapLots(List<Lot> lots) {
        if (lots == null) {
            return Collections.emptyList();
        }
        return lots.stream()
                .map(this::mapLot)
                .collect(Collectors.toList());
    }
}