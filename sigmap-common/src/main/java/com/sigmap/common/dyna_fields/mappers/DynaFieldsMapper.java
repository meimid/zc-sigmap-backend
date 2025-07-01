package com.sigmap.common.dyna_fields.mappers;

import com.sigmap.common.dyna_fields.DynaFields;
import com.sigmap.common.dyna_fields.request.DynaFieldsRequest;
import com.sigmap.common.dyna_fields.response.DynaFieldsDTO;
import com.sigmap.common.dyna_fields.response.DynaFieldsResponse;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import com.sigmap.common.dyna_fields_details.request.DynaFieldsDetailsRequest;
import java.util.List;
import java.util.Set;

import com.sigmap.common.dyna_fields_details.response.DynaFieldsDetailsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/** Mapper interface for converting between DynaField entities, requests, and responses. */
@Mapper(componentModel = "spring")
public interface DynaFieldsMapper {
  DynaFieldsMapper INSTANCE = Mappers.getMapper(DynaFieldsMapper.class);

  DynaFields map(DynaFieldsRequest source);

  @Mapping(target = "dynaFieldsId", ignore = true)
  @Mapping(target = "dynaDocsId", ignore = true)
  @Mapping(target = "dynaDocsVer", ignore = true)
  DynaFields mapWithoutId(DynaFields source);

  void map(DynaFieldsRequest source, @MappingTarget DynaFields target);

  DynaFieldsDetailsRequest map(DynaFieldsRequest.Detail source);

  List<DynaFieldsDetailsRequest> mapDetails(List<DynaFieldsRequest.Detail> source);

  Set<DynaFieldsDetails> detailsToSet(List<DynaFieldsDetails> source);

  @Mapping(target = "details", source = "dynaFieldsDetails")
  DynaFieldsResponse map(DynaFields source);

  @Mapping(target = "type", source = "fieldType")
  @Mapping(target = "sourceField", source = "source_field")
  DynaFieldsResponse.Detail map(DynaFieldsDetails source);

  List<DynaFieldsResponse> map(List<DynaFields> source);

  @Mapping(target = "dynaFieldsId", source = "dynaFieldsId")
  @Mapping(target = "dynaDocsId", source = "dynaDocsId")
  @Mapping(target = "dynaDocsVer", source = "dynaDocsVer")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "ref", source = "ref")
  @Mapping(target = "label", source = "label")
  @Mapping(target = "description", source = "description")
  @Mapping(target = "type", source = "type")
  @Mapping(target = "source", source = "source")
  @Mapping(target = "sourceField", source = "sourceField")
  @Mapping(target = "dynaFieldsOrder", source = "dynaFieldsOrder")
  @Mapping(target = "status", source = "status")
  DynaFieldsDTO toDynaFieldsDTO(DynaFields dynaFields);

  Set<DynaFieldsDetailsDTO> dynaFieldsDetailsToDTO (Set<DynaFieldsDetails> dynaFieldsDetails);
}
