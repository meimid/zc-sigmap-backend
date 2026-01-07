package com.sigmap.common.financement.mappers;

import com.sigmap.common.financement.request.FinancementRequest;
import com.sigmap.common.financement.request.FinancementBaseRequestDto;
import com.sigmap.common.financement.response.FinancementResponse;
import com.sigmap.common.financement.Financement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface FinancementMapper {

    FinancementMapper INSTANCE = Mappers.getMapper(FinancementMapper.class);

    /**
     * Maps Financement entity to FinancementResponse DTO
     */
    @Mappings({
            @Mapping(target = "financementId", source = "id.financementId"),
            @Mapping(target = "realisationId", source = "id.realisationId"),
            @Mapping(target = "realisationVer", source = "id.realisationVer"),
            @Mapping(target = "sourceFinancement", source = "sourceFinancement"),
            @Mapping(target = "budgetEstimer", source = "budgetEstimer"),
            @Mapping(target = "examenPrealable", source = "examenPrealable")

    })
    FinancementResponse mapToDto(Financement source);

    /**
     * Maps FinancementRequest to Financement entity
     * Note: The composite key and related entities need to be set separately in the service layer
     */
    @Mappings({
            @Mapping(target = "id", ignore = true), // Composite key set in service
            @Mapping(target = "sourceFinancement", source = "sourceFinancement"),
            @Mapping(target = "budgetEstimer", source = "budgetEstimer"),
            @Mapping(target = "examenPrealable", source = "examenPrealable"),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "updatedBy", ignore = true),
            @Mapping(target = "isDeleted", ignore = true)
    })
    Financement map(FinancementRequest source);

    /**
     * Updates existing Financement entity with data from FinancementBaseRequestDto
     */
    @Mappings({
            @Mapping(target = "id", ignore = true), // Don't update composite key
            @Mapping(target = "sourceFinancement", source = "sourceFinancement"),
            @Mapping(target = "budgetEstimer", source = "budgetEstimer"),
            @Mapping(target = "examenPrealable", source = "examenPrealable"),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "updatedAt", ignore = true), // Service layer handles these
            @Mapping(target = "updatedBy", ignore = true),
            @Mapping(target = "isDeleted", ignore = true)
    })
    void updateFinancementFromDto(FinancementBaseRequestDto source, @org.mapstruct.MappingTarget Financement target);
}
