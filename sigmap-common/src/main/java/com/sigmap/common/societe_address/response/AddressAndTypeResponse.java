package com.sigmap.common.societe_address.response;

import com.sigmap.common.adresse.response.AdresseResponseDto;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.Data;

@Data
public class AddressAndTypeResponse {
    private AdresseResponseDto address;
    private RefParamResponseDto type;
}
