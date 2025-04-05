package com.sigmap.common.agpm.mappers;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.agpm.response.AgpmResponse;

import com.sigmap.common.docs.Docs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AgpmMapper {
    AgpmMapper INSTANCE = Mappers.getMapper(AgpmMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "contenu", source = "contenu")
    @Mapping(target = "actId", source = "ac", qualifiedByName = "mapActId")
    @Mapping(target = "docIds", source = "docs", qualifiedByName = "mapDocIds")
    @Mapping(target = "statusId", source = "statusId")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    AgpmResponse map(Agpm agpm);

    @Named("mapActId")
    default Integer mapActId(Ac ac) {
        return ac != null ? ac.getId() : null;
    }

    @Named("mapDocIds")
    default List<Integer> mapDocIds(List<Docs> docs) {
        if (docs == null) {
            return null;
        }
        return docs.stream()
                .map(Docs::getDoc_id)
                .collect(Collectors.toList());
    }


    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "contenu", source = "contenu")
    @Mapping(target = "statusId", source = "statusId")
    @Mapping(target = "ac", ignore = true)
    @Mapping(target = "docs", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Agpm toEntity(AgpmRequest agpmRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "contenu", source = "contenu")
    @Mapping(target = "statusId", source = "statusId")
    @Mapping(target = "ac", ignore = true)
    @Mapping(target = "docs", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Agpm updateAgpmFromRequest(AgpmRequest request, @MappingTarget Agpm agpm);
}