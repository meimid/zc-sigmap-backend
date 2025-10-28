package com.sigmap.common.societe_dac.mappers;

import com.sigmap.common.societe_dac.SocieteDac;
import com.sigmap.common.societe_dac.request.SocieteDacRequest;
import com.sigmap.common.societe_dac.response.SocieteDacResponse;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SocieteDacMapper {

    SocieteDacMapper INSTANCE = Mappers.getMapper(SocieteDacMapper.class);


    @Mapping(target = "dacId", source = "id.dac.dacId")
    @Mapping(target = "dacVer", source = "id.dac.dacVer")
    /*@Mapping(target = "dac.reference", source = "id.dac.reference")
    @Mapping(target = "dac.dateLimiteSoumission", source = "id.dac.dateLimiteSoumission")
    @Mapping(target = "dac.dateLimiteRecptionDemande", source = "id.dac.dateLimiteRecptionDemande")
    @Mapping(target = "dac.payant", source = "id.dac.payant")
    @Mapping(target = "dac.shortListing", source = "id.dac.shortListing")
    @Mapping(target = "dac.parentDacId", source = "id.dac.parentDacId")*/

    //@Mapping(target = "dac", source = "id.dac")
    @Mapping(target = "societeId", source = "id.societe.id")
    SocieteDacResponse map(SocieteDac entity);


}
