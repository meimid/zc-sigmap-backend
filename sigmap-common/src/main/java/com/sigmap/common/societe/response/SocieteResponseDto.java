package com.sigmap.common.societe.response;

import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.Data;

@Data
public class SocieteResponseDto {
    private Integer id;
    private String nom;
    private RefParamResponseDto type;
    private RefParamResponseDto pays;
    private RefParamResponseDto categorieSoumissionnaire;
    private String nni;
}
