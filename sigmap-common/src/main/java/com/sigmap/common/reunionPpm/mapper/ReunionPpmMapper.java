package com.sigmap.common.reunionPpm.mapper;

import com.sigmap.common.ppms.mappers.PpmMapper;
import com.sigmap.common.reunionPpm.ReunionPpm;
import com.sigmap.common.reunionPpm.response.ReunionPpmResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PpmMapper.class})
public interface ReunionPpmMapper {
   ReunionPpmMapper INSTANCE = Mappers.getMapper(ReunionPpmMapper.class);

    @Mapping(source = "ppm", target = "ppm")
    ReunionPpmResponse toResponse(ReunionPpm reunionPpm);
    List<ReunionPpmResponse> toResponses(List<ReunionPpm> reunionPpm);
}
