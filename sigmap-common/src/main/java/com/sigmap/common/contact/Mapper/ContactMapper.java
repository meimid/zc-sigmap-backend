package com.sigmap.common.contact.Mapper;
import com.sigmap.common.contact.Contact;
import com.sigmap.common.contact.request.ContactRequest;
import com.sigmap.common.contact.response.ContactResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ContactMapper {
    ContactMapper INSTANCE = Mappers.getMapper(ContactMapper.class);
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    ContactResponse map(Contact source);
    Contact map(ContactRequest source);
}
