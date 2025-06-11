package com.sigmap.common.societe_address.response;

import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import com.sigmap.common.societe.response.SocieteResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SocieteAddressResponse {
    private Integer id;
    private RefParamResponseDto type;
    private String address;
    private SocieteResponseDto societe;
    private BaseEntityResponse metadata;

}
