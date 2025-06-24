package com.sigmap.common.pv_membre_present.response;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.Data;

@Data
public class PvMembrePresentResponseDto {
    private Integer pvMembrePresentId;
    private String nom;
    private String prenom;
    private boolean present;
    private RefParamResponseDto fonctionId;
    private String userId;
}
