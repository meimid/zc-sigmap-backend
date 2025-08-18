package com.sigmap.common.adresse.response;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class AdresseResponse {
    private Integer id;
    private String tel1;
    private String tel2;
    private String fax;
    private String adresse;
    private String compAdresse;
    private String codePostal;
    private String ville;
    private RefParamResponseDto pays;
    private BaseEntityResponse metadata;

}
