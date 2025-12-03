package com.sigmap.common.dac.mapper;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.mapper.AcMapper;
import com.sigmap.common.ac.response.AcWebsiteResponse;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.response.DacResponce;
import com.sigmap.common.dac.response.DacResponseReunion;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DacMapper {

    DacMapper INSTANCE = Mappers.getMapper(DacMapper.class);

    @Mapping(target = "ppm", source = "ppm")
    @Mapping(target = "typeMarche", source = "typeMarche")
    @Mapping(target = "modePassation", source = "modePassation")
    @Mapping(target = "realisation", source = "realisation")
    @Mapping(target = "reference", source = "reference")

    @Mapping(target = "dateLimiteSoumission", source = "dateLimiteSoumission")
    @Mapping(target = "dateLimiteRecptionDemande", source = "dateLimiteRecptionDemande")
    @Mapping(target = "payant", source = "payant")
    @Mapping(target = "dynaDocsModel", source = "dynaDocsModel")
    @Mapping(target = "etapeProcess", source = "etapeProcess")
    @Mapping(target = "processInstanceId", source = "processInstanceId")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    DacResponce map(Dac source);



    @Mapping(target = "ac", source = "ppm.ac")
    @Mapping(target = "reference", source = "reference")
    DacResponseReunion mapToDacResponseReunion(Dac source);

    @AfterMapping
    default void debugMapping(@MappingTarget DacResponseReunion target, Dac source) {
        System.out.println("=== DEBUG MAPPING ===");
        System.out.println("Source Dac ID: " + (source != null ? source.getDacId() : "null"));
        System.out.println("Source PPM: " + (source != null ? source.getPpm() : "null"));
        System.out.println("Source AC: " + (source != null && source.getPpm() != null ? source.getPpm().getAc() : "null"));
        System.out.println("Target AC: " + (target != null ? target.getAc() : "null"));
        System.out.println("=====================");
    }
}


