package com.sigmap.common.dac_doc_soumission.mapper;

import com.sigmap.common.dac_doc_soumission.DacDocSoumission;
import com.sigmap.common.dac_doc_soumission.request.DacDocSoumissionRequest;
import com.sigmap.common.dac_doc_soumission.response.DacDocSoumissionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
@Mapper(componentModel = "spring")

public interface DacDocSoumissionMapper {
    DacDocSoumissionMapper INSTANCE = Mappers.getMapper(DacDocSoumissionMapper.class);
    DacDocSoumission mapFromRequest(DacDocSoumissionRequest request);

    @Mapping(target = "dacId", source = "dacId")
    @Mapping(target = "docSoumission", source = "docSoumission")
    @Mapping(target = "dateValidate", source = "dateValidate")
    DacDocSoumissionResponse map(DacDocSoumission entity);
}
