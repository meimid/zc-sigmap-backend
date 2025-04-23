package com.sigmap.common.societe.response;

import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SocieteResponse {
    private Integer id;
    private String nom;
    private RefParamResponseDto type;
    private RefParamResponseDto pays;
    private String nni;
    private String categorieSoumissionnaireId;
    private BaseEntityResponse metadata;
}
