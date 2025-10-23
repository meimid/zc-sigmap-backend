package com.sigmap.common.user_societe.mapper;


import com.sigmap.common.societe.response.SocieteResponse;
import com.sigmap.common.societe.response.SocieteResponseDto;
import com.sigmap.common.user_societe.UserSociete;
import com.sigmap.common.user_societe.request.UserSocieteRequest;
import com.sigmap.common.user_societe.response.UserResponse;
import com.sigmap.common.user_societe.response.UserSocieteResponse;
import com.sigmap.common.user_societe.response.UserSocieteResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
@Mapper(componentModel = "spring")
public interface UserSocieteMapper {
    UserSocieteMapper INSTANCE = Mappers.getMapper(UserSocieteMapper.class);
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    UserSocieteResponse map(UserSociete userSociete);
    UserSociete mapFromRequest(UserSocieteRequest request);
    UserSocieteResponseDto toDto(UserSociete userSociete);
    UserResponse mapToUserDoResponse(UserSociete userSociete);


}
