package com.sigmap.common.reunionDac.mapper;

import com.sigmap.common.dac.mapper.DacMapper;
import com.sigmap.common.docs.Mapper.DocMapper;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.reunionDac.ReunionDac;
import com.sigmap.common.reunionDac.response.ReunionDacResponse;
import com.sigmap.common.reunion_dac_doc.ReunionDacDoc;
import com.sigmap.common.reunion_ppm_doc.ReunionPpmDoc;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {DacMapper.class})
public interface ReunionDacMapper {
    ReunionDacMapper INSTANCE = Mappers.getMapper(ReunionDacMapper.class);

    @Mapping(source = "dac", target = "dac")
    @Mapping(source ="dac.ppm.ac",target = "dac.ac")
    @Mapping(target = "docs", expression = "java(mapDocs(reunionDac.getReunionDacDocs()))")

    ReunionDacResponse toResponse(ReunionDac reunionDac);
    List<ReunionDacResponse> toResponses(List<ReunionDac> reunionDacs);
    default List<DocsResponseDTO> mapDocs(List<ReunionDacDoc> reunionDacDocs) {
        if (reunionDacDocs == null) return null;
        return reunionDacDocs.stream()
                .map(ReunionDacDoc::getDocs) // get the Docs entity
                .map(DocMapper.INSTANCE::mapToDto) // map to DTO using DocsMapper
                .collect(Collectors.toList());
    }
}
