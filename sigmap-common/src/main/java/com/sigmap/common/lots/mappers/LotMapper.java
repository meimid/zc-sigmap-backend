package com.sigmap.common.lots.mappers;

import com.sigmap.common.lots.Lot;
import com.sigmap.common.lots.request.LotRequest;
import com.sigmap.common.lots.response.LotResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LotMapper {
    LotMapper INSTANCE = Mappers.getMapper(LotMapper.class);


//    @Mapping(target = "id", source = "id")
//    @Mapping(target = "version", source = "version")
//    @Mapping(target = "description", source = "description")
    @Mapping(target = "budget", source = "budget")
    @Mapping(target = "realisations",ignore = true)
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    LotResponse map(Lot source);
    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "budget", source = "budget")
    @Mapping(target = "realisations", ignore = true)
    Lot toEntity(LotRequest lotRequest);
}
