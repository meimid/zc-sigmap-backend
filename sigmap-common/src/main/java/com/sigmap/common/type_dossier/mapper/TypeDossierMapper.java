package com.sigmap.common.type_dossier.mapper;

import com.sigmap.common.type_dossier.TypeDossier;
import com.sigmap.common.type_dossier.response.TypeDossierResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")

public interface TypeDossierMapper {
    TypeDossierMapper INSTANCE = Mappers.getMapper(TypeDossierMapper.class);
    TypeDossierResponse map(TypeDossier typeDossier);

}
