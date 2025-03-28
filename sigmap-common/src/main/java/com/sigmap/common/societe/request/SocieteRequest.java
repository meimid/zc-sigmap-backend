package com.sigmap.common.societe.request;

import com.sigmap.common.ref_params.RefParam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SocieteRequest {
    private Integer id;
    private String nom;
    private Integer typeId;
    private Integer paysId;
    private String nni;
    private String categorieSoumissionnaireId;
}
