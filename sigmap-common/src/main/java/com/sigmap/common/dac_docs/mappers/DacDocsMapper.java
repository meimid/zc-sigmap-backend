package com.sigmap.common.dac_docs.mappers;

import com.sigmap.common.dac_docs.DacDocs;
import com.sigmap.common.dac_docs.request.DacDocsRequest;
import com.sigmap.common.dac_docs.response.DacDocsResponse;
import com.sigmap.common.dac_docs.response.DacDocsResponseFullEntities;
import com.sigmap.common.dac_docs_details.DacDocsDetails;
import com.sigmap.common.dac_docs_details.request.DacDocsDetailsRequest;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import com.sigmap.common.dyna_fields_details.response.DynaFieldsDetailsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface DacDocsMapper {
    DacDocsMapper INSTANCE = Mappers.getMapper(DacDocsMapper.class);

    /*@Mapping(target = "id.dacId", source = "dacId")
    @Mapping(target = "id.dynaFieldsId", source = "dynaFieldsId")
    @Mapping(target = "id.dynaDocsId", source = "dynaDocsId")
    @Mapping(target = "sourceId", source = "sourceId")
    @Mapping(target = "fieldValue", source = "fieldValue")*/
    DacDocs toEntity(DacDocsRequest request);




    @Mapping(target = "dacId", source = "id.dacId")
    @Mapping(target = "dacVer", source = "id.dacVer")
    @Mapping(target = "dynaFieldsId", source = "id.dynaFieldsId")
    @Mapping(target = "dynaDocsId", source = "id.dynaDocsId")
    @Mapping(target = "dynaDocsVer", source = "id.dynaDocsVer")
    @Mapping(target = "sourceId", source = "sourceId")
    @Mapping(target = "fieldValue", source = "fieldValue")
    DacDocsResponse toResponse (DacDocs entity);


    @Mapping(target = "dacId", source = "id.dacId")
    @Mapping(target = "dacVer", source = "id.dacVer")
    List<DacDocsResponseFullEntities> entityToResponse (List<DacDocs> entity);

    @Mapping(target = "dacId", source = "id.dacId")
    @Mapping(target = "dacVer", source = "id.dacVer")
    DacDocsResponseFullEntities entityToResponseMap (DacDocs entity);

     Set<DynaFieldsDetailsDTO> dynaFieldsDetailsToDTO (Set<DynaFieldsDetails> dynaFieldsDetails);


    List<DacDocsDetailsRequest> mapDetails(List<DacDocsRequest.DacDocsDetail> source);

    Set<DacDocsDetails> detailsToSet(List<DacDocsDetails> source);
} 