package com.sigmap.common.dac_docs_details.mappers;

import com.sigmap.common.dac_docs_details.DacDocsDetails;
import com.sigmap.common.dac_docs_details.request.DacDocsDetailsRequest;
import com.sigmap.common.dac_docs_details.response.DacDocsDetailsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DacDocsDetailsMapper {
    DacDocsDetailsMapper INSTANCE = Mappers.getMapper(DacDocsDetailsMapper.class);

    DacDocsDetails toEntity(DacDocsDetailsRequest request);
    DacDocsDetailsRequest toRequest(DacDocsDetails entity);
    DacDocsDetailsResponse toResponse(DacDocsDetails entity);
} 