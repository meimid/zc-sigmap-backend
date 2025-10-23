package com.sigmap.common.societe.mappers;


import com.sigmap.common.adresse.response.AdresseResponseDto;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.mappers.RefParamMapper;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.request.SocieteRequest;
import com.sigmap.common.societe.request.SocieteRequestInscription;
import com.sigmap.common.societe.response.SocieteResponse;
import com.sigmap.common.societe.response.SocieteResponseDto;
import com.sigmap.common.societe_address.SocieteAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Use MapStruct to generate code for mapping between Objects.
 *
 * @see <a href="https://mapstruct.org/documentation/stable/reference/html/">MapStruct Documentation</a>
 */
@Mapper
public interface SocieteMapper {
    SocieteMapper INSTANCE = Mappers.getMapper(SocieteMapper.class);

    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "societeAddresses", source = "societeAddresses", qualifiedByName = "mapSocieteAddresses")

    SocieteResponse map(Societe source);
    Societe map(SocieteRequest source);
    Societe map(SocieteRequestInscription source);
    @Mapping(target = "metadata.createdAt", ignore = true)
    @Mapping(target = "metadata.createdBy", ignore = true)
    @Mapping(target = "metadata.updatedAt", ignore = true)
    @Mapping(target = "metadata.updatedBy", ignore = true)
    @Mapping(target = "metadata.deletedBy", ignore = true)
    @Mapping(target = "metadata.isDeleted", ignore = true)
    void updateFromRequest(SocieteRequest source, @MappingTarget Societe target);

    SocieteResponseDto toResponseDto(Societe source);

    @Named("mapSocieteAddresses")
    default List<AdresseResponseDto> mapSocieteAddresses(List<SocieteAddress> societeAddresses) {
        if (societeAddresses == null) return null;

        return societeAddresses.stream()
                .map(sa -> {
                    var dto = new AdresseResponseDto();

                    if (sa.getAddress() != null) {
                        var adresse = sa.getAddress();
                        dto.setId(adresse.getId());
                        dto.setEmail(adresse.getEmail());
                        dto.setTel1(adresse.getTel1());
                        dto.setTel2(adresse.getTel2());
                        dto.setFax(adresse.getFax());
                        dto.setAdresse(adresse.getAdresse());
                        dto.setCompAdresse(adresse.getCompAdresse());
                        dto.setCodePostal(adresse.getCodePostal());
                        dto.setVille(adresse.getVille());
                        dto.setPays(RefParamMapper.INSTANCE.mapToDto(adresse.getPays()));
                    } else {
                        dto.setId(sa.getAddressId());
                        dto.setEmail(sa.getAddress().getEmail());




                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

}

