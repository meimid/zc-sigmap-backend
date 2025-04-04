package com.sigmap.common.compte_bancaire.mapper;
import com.sigmap.common.compte_bancaire.CompteBancaire;
import com.sigmap.common.compte_bancaire.response.CompteBancaireResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CompteBancaireMapper {
    CompteBancaireMapper INSTANCE = Mappers.getMapper(CompteBancaireMapper.class);
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    CompteBancaireResponse map(CompteBancaire source);
}
