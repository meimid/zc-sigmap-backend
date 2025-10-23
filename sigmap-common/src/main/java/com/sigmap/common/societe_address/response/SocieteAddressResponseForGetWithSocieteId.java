package com.sigmap.common.societe_address.response;

import com.sigmap.common.adresse.response.AdresseResponseDto;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.societe.response.SocieteResponseDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SocieteAddressResponseForGetWithSocieteId {
    private SocieteResponseDto societe;
    List<AddressAndTypeResponse> content;

}
