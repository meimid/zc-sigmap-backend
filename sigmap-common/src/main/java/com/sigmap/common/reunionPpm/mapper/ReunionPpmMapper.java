package com.sigmap.common.reunionPpm.mapper;

import com.sigmap.common.docs.Mapper.DocMapper;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.ppms.mappers.PpmMapper;
import com.sigmap.common.reunionPpm.ReunionPpm;
import com.sigmap.common.reunionPpm.response.ReunionPpmResponse;
import com.sigmap.common.reunion_ppm_doc.ReunionPpmDoc;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {PpmMapper.class})
public interface ReunionPpmMapper {
   ReunionPpmMapper INSTANCE = Mappers.getMapper(ReunionPpmMapper.class);

    @Mapping(source = "ppm", target = "ppm")
    @Mapping(target = "docs", expression = "java(mapDocs(reunionPpm.getReunionPpmDocs()))")
    ReunionPpmResponse toResponse(ReunionPpm reunionPpm);
    List<ReunionPpmResponse> toResponses(List<ReunionPpm> reunionPpm);

    default List<DocsResponseDTO> mapDocs(List<ReunionPpmDoc> reunionPpmDocs) {
        if (reunionPpmDocs == null) return null;
        return reunionPpmDocs.stream()
                .map(ReunionPpmDoc::getDocs) // get the Docs entity
                .map(DocMapper.INSTANCE::mapToDto) // map to DTO using DocsMapper
                .collect(Collectors.toList());
    }
}
