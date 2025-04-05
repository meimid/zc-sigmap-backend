package com.sigmap.common.societe_address.response;

import com.sigmap.common.ref_params.response.RefParamResponse;
import com.sigmap.common.response.BaseEntityResponse;
import com.sigmap.common.societe.response.SocieteResponse;
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
    private RefParamResponse type;
    private String address;
    private SocieteResponse societe;
    private BaseEntityResponse metadata;

}
