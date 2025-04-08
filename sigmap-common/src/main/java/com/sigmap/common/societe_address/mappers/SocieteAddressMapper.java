package com.sigmap.common.societe_address.mappers;

import com.sigmap.common.societe_address.SocieteAddress;
import com.sigmap.common.societe_address.request.SocieteAddressRequest;
import com.sigmap.common.societe_address.response.SocieteAddressResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
/**
 * Use MapStruct to generate code for mapping between Objects.
 *
 * @see <a href="https://mapstruct.org/documentation/stable/reference/html/">MapStruct Documentation</a>
 */
@Mapper
public interface SocieteAddressMapper {
    SocieteAddressMapper INSTANCE = Mappers.getMapper(SocieteAddressMapper.class);

    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    SocieteAddressResponse map(SocieteAddress source);
    SocieteAddress map(SocieteAddressRequest source);
}
