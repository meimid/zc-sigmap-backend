package com.sigmap.common.societe_address.mappers;


import com.sigmap.common.societe_address.SocieteAddress;
import com.sigmap.common.societe_address.request.SocieteAddressRequest;
import com.sigmap.common.societe_address.request.SocieteAddressUpdateRequest;
import com.sigmap.common.societe_address.request.SocieteAdressUnderSocieteRequest;
import com.sigmap.common.societe_address.response.SocieteAddressResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
/**
 * Use MapStruct to generate code for mapping between Objects.
 *
 * @see <a href="https://mapstruct.org/documentation/stable/reference/html/">MapStruct Documentation</a>
 */
@Mapper
public interface SocieteAddressMapper {
    SocieteAddressMapper INSTANCE = Mappers.getMapper(SocieteAddressMapper.class);
    SocieteAddressResponse map(SocieteAddress source);
    SocieteAddress map(SocieteAddressRequest source);
    SocieteAddress mapFromSocieteAddressUnderSocieteRequest(SocieteAdressUnderSocieteRequest source);
    void updateFromRequest(SocieteAddressUpdateRequest source, @MappingTarget SocieteAddress target);
}
