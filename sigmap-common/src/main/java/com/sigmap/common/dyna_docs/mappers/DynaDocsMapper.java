package com.sigmap.common.dyna_docs.mappers;

import com.sigmap.common.dac_docs.response.customResponceDTO.DynaDocsResponseDTO;
import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_docs.request.DynaDocsRequest;
import com.sigmap.common.dyna_docs.response.DynaDocsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/** Mapper interface for converting between DynaDoc entities, requests, and responses. */
@Mapper(componentModel = "spring")
public interface DynaDocsMapper {
  DynaDocsMapper INSTANCE = Mappers.getMapper(DynaDocsMapper.class);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  DynaDocs mapWithoutId(DynaDocs source);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "typeMarche", ignore = true)
  @Mapping(target = "modePassation", ignore = true)
  DynaDocs map(DynaDocsRequest source);

  @Mapping(target = "typeMarche", ignore = true)
  @Mapping(target = "modePassation", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  void map(DynaDocsRequest source, @MappingTarget DynaDocs target);

  @Mapping(target = "typeMarche", source = "typeMarche")
  @Mapping(target = "modePassation", source = "modePassation")
  @Mapping(target = "isStatic", source = "isStatic")
  DynaDocsResponse map(DynaDocs source);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "version", source = "version")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "templateFileName", source = "templateFileName")
  @Mapping(target = "guideFileName", source = "guideFileName")
  @Mapping(target = "typeMarche", source = "typeMarche")
  @Mapping(target = "modePassation", source = "modePassation")
  @Mapping(target = "dynaDocOrder", source = "dynaDocOrder")
  @Mapping(target = "status", source = "status")
  @Mapping(target = "isStatic", source = "isStatic")
  DynaDocsResponseDTO toDynaDocsResponse(DynaDocs dynaDocs);
}
