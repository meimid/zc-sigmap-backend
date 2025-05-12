package com.sigmap.common.societe_address.response;

import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.societe.response.SocieteResponseDto;
import lombok.Data;

@Data
public class SocieteAdressResponseDto {
    private Integer id;
    private RefParamResponseDto type;
    private String address;

}
