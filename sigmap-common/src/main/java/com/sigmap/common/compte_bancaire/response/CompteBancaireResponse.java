package com.sigmap.common.compte_bancaire.response;
import com.sigmap.common.ref_params.response.RefParamResponse;
import com.sigmap.common.response.BaseEntityResponse;
import com.sigmap.common.societe.response.SocieteResponse;
import lombok.Data;

@Data
public class CompteBancaireResponse {
    private Integer id;
    private String nom;
    private String agence;
    private String rib;
    private RefParamResponse pays;
    private boolean local;
    private SocieteResponse societe;
    private BaseEntityResponse metadata;

}
