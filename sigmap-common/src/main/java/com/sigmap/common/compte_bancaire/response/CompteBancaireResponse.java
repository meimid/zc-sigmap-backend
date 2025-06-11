package com.sigmap.common.compte_bancaire.response;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import com.sigmap.common.societe.response.SocieteResponseDto;
import lombok.Data;

@Data
public class CompteBancaireResponse {
    private Integer id;
    private String nom;
    private String agence;
    private String rib;
    private RefParamResponseDto pays;
    private boolean local;
    private SocieteResponseDto societe;
    private BaseEntityResponse metadata;

}
