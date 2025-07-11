package com.sigmap.common.process_verbal.mappers;

import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.ppms.mappers.PpmMapper;
import com.sigmap.common.process_verbal.ProcessVerbal;
import com.sigmap.common.process_verbal.request.ProcessVerbalRequest;
import com.sigmap.common.process_verbal.response.ProcessVerbalResponse;
import com.sigmap.common.pv_docs.PvDocs;
import com.sigmap.common.pv_docs.response.PvDocsResponseDto;
import com.sigmap.common.pv_membre_present.response.PvMembrePresentResponseDto;
import com.sigmap.common.ref_params.mappers.RefParamMapper;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    @Mapping(target = "documents",source = "documents")
    ProcessVerbalResponse map(ProcessVerbal source);

    /*Set<PvDocsResponseDto> mapDocuments(Set<PvDocs> source);

    PvDocsResponseDto map(PvDocs source);*/

}
