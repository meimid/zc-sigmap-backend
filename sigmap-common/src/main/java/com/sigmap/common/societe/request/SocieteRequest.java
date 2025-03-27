package com.sigmap.common.societe.request;

import com.sigmap.common.ref_params.RefParam;

import lombok.Data;

@Data
public class SocieteRequest {
    private String nom;
    private Integer typeId;
    private Integer paysId;
    private String nni;
    private String categorieSoumissionnaireId;
}
