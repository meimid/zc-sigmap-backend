package com.sigmap.common.process_verbal.mappers;

import com.sigmap.common.ppms.mappers.PpmMapper;
import com.sigmap.common.process_verbal.ProcessVerbal;

import com.sigmap.common.process_verbal.response.ProcessVerbalResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")

public interface ProcessVerbalMapper {
    ProcessVerbalMapper INSTANCE = Mappers.getMapper(ProcessVerbalMapper.class);

    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "documents", source = "documents")
    ProcessVerbalResponse map(ProcessVerbal source);

    /*
     * Set<PvDocsResponseDto> mapDocuments(Set<PvDocs> source);
     * 
     * PvDocsResponseDto map(PvDocs source);
     */

}
