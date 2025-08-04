package com.sigmap.common.reunionDac.mapper;

import com.sigmap.common.dac.mapper.DacMapper;
import com.sigmap.common.reunionDac.ReunionDac;
import com.sigmap.common.reunionDac.response.ReunionDacResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DacMapper.class})
public interface ReunionDacMapper {
    ReunionDacMapper INSTANCE = Mappers.getMapper(ReunionDacMapper.class);

    @Mapping(source = "dac", target = "dac")
    ReunionDacResponse toResponse(ReunionDac reunionDac);
    List<ReunionDacResponse> toResponses(List<ReunionDac> reunionDacs);
}
