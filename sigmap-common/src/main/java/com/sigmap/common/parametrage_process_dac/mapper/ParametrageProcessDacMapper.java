package com.sigmap.common.parametrage_process_dac.mapper;

import com.sigmap.common.parametrage_process_dac.ParametrageProcessDac;
import com.sigmap.common.parametrage_process_dac.response.ParametrageProcessDacResonse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ParametrageProcessDacMapper {
    ParametrageProcessDacMapper INSTANCE = Mappers.getMapper(ParametrageProcessDacMapper.class);


    ParametrageProcessDacResonse map(ParametrageProcessDac acProcess);
}
