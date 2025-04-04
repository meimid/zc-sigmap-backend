package com.sigmap.common.societe.response;

import com.sigmap.common.ref_params.response.RefParamResponse;
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
    private RefParamResponse type;
    private RefParamResponse pays;
    private String nni;
    private String categorieSoumissionnaireId;
    private BaseEntityResponse metadata;
}
