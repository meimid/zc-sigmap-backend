package com.sigmap.common.seuil_general.mapper;

import com.sigmap.common.seuil_general.SeuilGeneral;
import com.sigmap.common.seuil_general.response.SeuilGeneralResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SeuilGeneralMapper {
    SeuilGeneralMapper INSTANCE = Mappers.getMapper(SeuilGeneralMapper.class);

    SeuilGeneralResponse map(SeuilGeneral seuilGeneral);
}
