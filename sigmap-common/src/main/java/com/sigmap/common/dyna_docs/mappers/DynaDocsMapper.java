package com.sigmap.common.dyna_docs.mappers;

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

  @Mapping(target = "dynaDocsId", ignore = true)
  @Mapping(target = "dynaDocsVer", ignore = true)
  DynaDocs mapWithoutId(DynaDocs source);

  @Mapping(target = "dynaDocsId", ignore = true)
  @Mapping(target = "dynaDocsVer", ignore = true)
  @Mapping(target = "categorieId", ignore = true)
  DynaDocs map(DynaDocsRequest source);

  @Mapping(target = "categorieId", ignore = true)
  @Mapping(target = "dynaDocsId", ignore = true)
  @Mapping(target = "dynaDocsVer", ignore = true)
  void map(DynaDocsRequest source, @MappingTarget DynaDocs target);

  @Mapping(target = "categorie", source = "categorieId")
  DynaDocsResponse map(DynaDocs source);
}
