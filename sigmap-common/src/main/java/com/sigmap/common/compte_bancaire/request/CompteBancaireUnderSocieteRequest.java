package com.sigmap.common.compte_bancaire.request;

import lombok.Data;

@Data
public class CompteBancaireUnderSocieteRequest {
    private String nom;
    private String agence;
    private String rib;
    private Integer paysId;
    private boolean local;
}
