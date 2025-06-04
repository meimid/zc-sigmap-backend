package com.sigmap.common.dyna_fields_details.mappers;

import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import com.sigmap.common.dyna_fields_details.request.DynaFieldsDetailsRequest;
import com.sigmap.common.dyna_fields_details.response.DynaFieldsDetailsResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DynaFieldsDetailsMapper {
  DynaFieldsDetailsMapper INSTANCE = Mappers.getMapper(DynaFieldsDetailsMapper.class);

  @Mapping(target = "typeId", ignore = true)
  DynaFieldsDetails map(DynaFieldsDetailsRequest source);

  DynaFieldsDetailsResponse map(DynaFieldsDetails source);

  List<DynaFieldsDetailsResponse> map(List<DynaFieldsDetails> source);
}
