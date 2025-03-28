package com.sigmap.common.compte_bancaire.request;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.societe.Societe;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class CompteBancaireRequest {
    private Integer id;
    private String nom;
    private String agence;
    private String rib;
    private Integer paysId;
    private boolean local;
    private Integer societeId;
}
