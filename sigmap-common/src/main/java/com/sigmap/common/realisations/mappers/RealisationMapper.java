package com.sigmap.common.realisations.mappers;

import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.request.RealisationRequest;
import com.sigmap.common.realisations.response.RealisationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RealisationMapper {
    RealisationMapper INSTANCE = Mappers.getMapper(RealisationMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "realisationEnvisagees", source = "realisationEnvisagees")
    @Mapping(target = "serviceId", source = "serviceId")
    @Mapping(target = "directionId", source = "directionId")
    @Mapping(target = "sourceFinancementId", source = "sourceFinancementId")
    @Mapping(target = "monaisId", source = "monaisId")
    @Mapping(target = "typeMarcherId", source = "typeMarcherId")
    @Mapping(target = "modeSelectionId", source = "modeSelectionId")
    @Mapping(target = "datePrevLps", source = "datePrevLps")
    @Mapping(target = "datePrevAc", source = "datePrevAc")
    @Mapping(target = "datePrevDp", source = "datePrevDp")
    @Mapping(target = "datePrevAp", source = "datePrevAp")
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "isPublished", source = "isPublished")
    @Mapping(target = "publishedAt", source = "publishedAt")
    @Mapping(target = "sort", source = "sort")
    @Mapping(target = "paas", source = "paas") // Maps the full list
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    RealisationResponse map(Realisation source);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "realisationEnvisagees", source = "realisationEnvisagees")
    @Mapping(target = "serviceId", source = "serviceId")
    @Mapping(target = "directionId", source = "directionId")
    @Mapping(target = "sourceFinancementId", source = "sourceFinancementId")
    @Mapping(target = "monaisId", source = "monaisId")
    @Mapping(target = "typeMarcherId", source = "typeMarcherId")
    @Mapping(target = "modeSelectionId", source = "modeSelectionId")
    @Mapping(target = "datePrevLps", source = "datePrevLps")
    @Mapping(target = "datePrevAc", source = "datePrevAc")
    @Mapping(target = "datePrevDp", source = "datePrevDp")
    @Mapping(target = "datePrevAp", source = "datePrevAp")
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "isPublished", source = "isPublished")
    @Mapping(target = "publishedAt", source = "publishedAt")
    @Mapping(target = "sort", source = "sort")
    @Mapping(target = "paas", ignore = true) // Ignored as it’s typically set separately
    @Mapping(target = "lots", ignore = true) // Ignored as it’s typically set separately
    @Mapping(target = "ppms", ignore = true) // Ignored as it’s typically set separately
    @Mapping(target = "docs", ignore = true) // Ignored as it’s typically set separately
    Realisation toEntity(RealisationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    Realisation updateRealisationFromRequest(RealisationRequest request, @MappingTarget Realisation realisation);

}