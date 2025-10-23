package com.sigmap.common.adresse.response;

import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.Data;

@Data
public class AdresseResponseDto {
    private Integer id;
    private String email;
    private String tel1;
    private String tel2;
    private String fax;
    private String adresse;
    private String compAdresse;
    private String codePostal;
    private String ville;
    private RefParamResponseDto pays;
}
