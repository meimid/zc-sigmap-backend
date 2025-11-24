package com.sigmap.common.user_com.mappers;

import com.sigmap.common.keycloakAdmin.dto.ComMemberDTO;
import com.sigmap.common.user_com.UserCom;
import com.sigmap.common.user_com.response.UserComResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserComMpapper {
    UserComMpapper INSTANCE = Mappers.getMapper(UserComMpapper.class);
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    UserComResponse map(UserCom source);
    @Mapping(source = "userId", target = "id")
    ComMemberDTO mapUserToDto(UserCom source);
    List<ComMemberDTO> mapUsersToDto(List<UserCom> source);
}
