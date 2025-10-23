package com.sigmap.common.dac_prix.mapper;

import com.sigmap.common.dac_prix.DacPrix;
import com.sigmap.common.dac_prix.response.DacPrixResponce;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DacPrixMapper {
    DacPrixMapper INSTANCE = Mappers.getMapper(DacPrixMapper.class);


    @Mapping(target = "dacId", source = "id.dac.dacId")
    @Mapping(target = "dacVer", source = "id.dac.dacVer")
    @Mapping(target = "monnaie", source = "id.monnaie")
    DacPrixResponce map(DacPrix entity);

}
