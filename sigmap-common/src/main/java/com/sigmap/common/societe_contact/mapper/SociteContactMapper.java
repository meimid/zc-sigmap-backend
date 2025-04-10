package com.sigmap.common.societe_contact.mapper;

import com.sigmap.common.societe_contact.Request.SocieteContactRequest;
import com.sigmap.common.societe_contact.Response.SocieteContactResponse;
import com.sigmap.common.societe_contact.SocieteContact;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SociteContactMapper {

    SociteContactMapper INSTANCE = Mappers.getMapper(SociteContactMapper.class);
    @Mapping(target = "societe", source = "societeContactId.societe")
    @Mapping(target = "contact", source = "societeContactId.contact")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    SocieteContactResponse map(SocieteContact source);
    SocieteContact map(SocieteContactRequest source);
}
