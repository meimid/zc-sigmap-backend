package com.sigmap.common.categorie.mappers;

import com.sigmap.common.categorie.Categorie;
import com.sigmap.common.categorie.request.CategorieRequest;
import com.sigmap.common.categorie.response.CategorieResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface CategorieMapper {

    CategorieMapper INSTANCE = Mappers.getMapper(CategorieMapper.class);


    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    CategorieResponse map(Categorie source);

    Categorie toEntity(CategorieRequest request);

    void updateCategorieFromRequest(CategorieRequest request, @MappingTarget Categorie categorie);

}
