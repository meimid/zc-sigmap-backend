package com.sigmap.common.source_financement.mappers;

import com.sigmap.common.source_financement.SourceFinancement;
import com.sigmap.common.source_financement.request.SourceFinancementRequest;
import com.sigmap.common.source_financement.response.SourceFinancementResponse;
import com.sigmap.common.source_financement.response.SourceFinancementResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * Use MapStruct to generate code for mapping between Objects.
 *
 * @see <a href=
 *      "https://mapstruct.org/documentation/stable/reference/html/">MapStruct
 *      Documentation</a>
 */

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SourceFinancementMapper {
    SourceFinancementMapper INSTANCE = Mappers.getMapper(SourceFinancementMapper.class);

    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    SourceFinancementResponse map(SourceFinancement source);

    SourceFinancement map(SourceFinancementRequest source);

    void updateFromRequest(SourceFinancementRequest source, @MappingTarget SourceFinancement target);

    SourceFinancementResponseDto mapToDto(SourceFinancement source);
}
