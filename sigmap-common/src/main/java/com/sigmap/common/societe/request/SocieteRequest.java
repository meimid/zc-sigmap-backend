package com.sigmap.common.societe.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SocieteRequest {
    private String nom;
    private Integer typeId;
    private Integer paysId;
    private String nni;
    private String categorieSoumissionnaireId;
}
