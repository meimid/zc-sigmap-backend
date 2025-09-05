package com.sigmap.common.compte_bancaire.response;

import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.Data;

@Data
public class CompteBancaireResponseDto {
    private Integer id;
    private String nom;
    private String agence;
    private String rib;
    private RefParamResponseDto pays;
    private boolean local;
}
