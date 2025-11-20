package com.sigmap.common.doc_soumission.mapper;


import com.sigmap.common.doc_soumission.DocSoumission;
import com.sigmap.common.doc_soumission.request.DocSoumissionRequest;
import com.sigmap.common.doc_soumission.response.DocSoumissionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")

public interface DocSoumissionMapper {
    DocSoumissionMapper INSTANCE = Mappers.getMapper(DocSoumissionMapper.class);

    DocSoumissionResponse map(DocSoumission DocSoumission);
    DocSoumission mapFromRequest(DocSoumissionRequest request);
}
