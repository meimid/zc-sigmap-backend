package com.sigmap.common.dyna_fields.mappers;

import com.sigmap.common.dyna_fields.DynaFields;
import com.sigmap.common.dyna_fields.request.DynaFieldsRequest;
import com.sigmap.common.dyna_fields.response.DynaFieldsResponse;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import com.sigmap.common.dyna_fields_details.request.DynaFieldsDetailsRequest;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/** Mapper interface for converting between DynaField entities, requests, and responses. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DynaFieldsMapper {
  DynaFieldsMapper INSTANCE = Mappers.getMapper(DynaFieldsMapper.class);

  DynaFields map(DynaFieldsRequest source);

  DynaFieldsDetailsRequest map(DynaFieldsRequest.Detail source);

  List<DynaFieldsDetailsRequest> mapDetails(List<DynaFieldsRequest.Detail> source);

  Set<DynaFieldsDetails> detailsToSet(List<DynaFieldsDetails> source);

  @Mapping(target = "details", source = "dynaFieldsDetails")
  DynaFieldsResponse map(DynaFields source);

  DynaFieldsResponse.Detail map(DynaFieldsDetails source);

  List<DynaFieldsResponse> map(List<DynaFields> source);
}
