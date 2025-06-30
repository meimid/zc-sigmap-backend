package com.sigmap.common.dac_docs.mappers;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac_docs.DacDocs;
import com.sigmap.common.dac_docs.request.DacDocsRequest;
import com.sigmap.common.dac_docs.response.DacDocsResponse;
import com.sigmap.common.dac_docs.response.DacDocsResponseFullEntities;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacResponseDTO;
import com.sigmap.common.dac_docs.response.customResponceDTO.DynaDocsResponseDTO;
import com.sigmap.common.dac_docs_details.DacDocsDetails;
import com.sigmap.common.dac_docs_details.request.DacDocsDetailsRequest;
import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_fields.response.DynaFieldsDTO;
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

    // costum responce
    @Mapping(target = "dacId", source = "dacId")
    @Mapping(target = "dacVer", source = "dacVer")
    DacResponseDTO toDacResponse(Dac dac);

    @Mapping(target = "id", source = "dynaField.dynaDocs.id")
    @Mapping(target = "version", source = "dynaField.dynaDocs.version")
    @Mapping(target = "name", source = "dynaField.dynaDocs.name")
    @Mapping(target = "templateFileName", source = "dynaField.dynaDocs.templateFileName")
    @Mapping(target = "guideFileName", source = "dynaField.dynaDocs.guideFileName")
    @Mapping(target = "categorieId", source = "dynaField.dynaDocs.categorieId")
    @Mapping(target = "dynaDocOrder", source = "dynaField.dynaDocs.dynaDocOrder")
    @Mapping(target = "status", source = "dynaField.dynaDocs.status")
    DynaDocsResponseDTO toDynaDocsResponse(DacDocs dacDocs);

    @Mapping(target = "dynaFieldsId", source = "dynaField.dynaFieldsId")
    @Mapping(target = "dynaDocsId", source = "dynaField.dynaDocsId")
    @Mapping(target = "dynaDocsVer", source = "dynaField.dynaDocsVer")
    @Mapping(target = "name", source = "dynaField.name")
    @Mapping(target = "ref", source = "dynaField.ref")
    @Mapping(target = "label", source = "dynaField.label")
    @Mapping(target = "description", source = "dynaField.description")
    @Mapping(target = "type", source = "dynaField.type")
    @Mapping(target = "source", source = "dynaField.source")
    @Mapping(target = "sourceField", source = "dynaField.sourceField")
    @Mapping(target = "dynaFieldsOrder", source = "dynaField.dynaFieldsOrder")
    @Mapping(target = "status", source = "dynaField.status")
    DynaFieldsDTO toDynaFieldResponse(DacDocs dacDocs);
} 