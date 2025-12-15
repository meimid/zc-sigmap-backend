package com.sigmap.common.societe_blak_list.mappers;

import com.sigmap.common.societe_blak_list.SocieteBlackList;
import com.sigmap.common.societe_blak_list.request.SocieteBlackListRequest;
import com.sigmap.common.societe_blak_list.response.SocieteBlackListResponse;
import com.sigmap.common.societe_blak_list.response.SocieteBlackListResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(
        componentModel = "spring"

)
public interface SocieteBlackListMapper {
    SocieteBlackListMapper INSTANCE = Mappers.getMapper(SocieteBlackListMapper.class);
    @Mapping(target = "nom", source = "societe.nom")
    @Mapping(target = "nif", source = "societe.nif")
    @Mapping(target = "pays", source = "societe.address.pays.description")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    SocieteBlackListResponse toResponse(SocieteBlackList source);
    SocieteBlackList map(SocieteBlackListRequest source);
    @Mapping(target = "nom", source = "societe.nom")
    @Mapping(target = "nif", source = "societe.nif")
    @Mapping(target = "pays", source = "societe.address.pays.description")
    SocieteBlackListResponseDto toResponseDto(SocieteBlackList source);
    void updateFromRequest(SocieteBlackListRequest request, @MappingTarget SocieteBlackList target);




}
