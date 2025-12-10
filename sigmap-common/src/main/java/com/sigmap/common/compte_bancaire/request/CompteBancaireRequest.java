package com.sigmap.common.compte_bancaire.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompteBancaireRequest {
    private String nom;
    private String agence;
    private String rib;
    private Integer paysId;
    private boolean local;
    private Integer societeId;
}
