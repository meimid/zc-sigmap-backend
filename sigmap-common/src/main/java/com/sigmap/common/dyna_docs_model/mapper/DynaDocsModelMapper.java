package com.sigmap.common.dyna_docs_model.mapper;

import com.sigmap.common.doc_soumission.DocSoumission;
import com.sigmap.common.doc_soumission.response.DocSoumissionResponse;
import com.sigmap.common.dyna_docs_model.DynaDocsModel;
import com.sigmap.common.dyna_docs_model.response.DynaDocsModelResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DynaDocsModelMapper {
    DynaDocsModelMapper INSTANCE = Mappers.getMapper(DynaDocsModelMapper.class);


    DynaDocsModelResponse map(DynaDocsModel dynaDocsModel);
}
