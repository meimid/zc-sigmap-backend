package com.sigmap.common.societe_dac.response;

import com.sigmap.common.entity.MethodePaiement;
import lombok.Data;

@Data
public class SocieteDacResponsePayment {

    private SocieteResponseForPaymentDTO societe;
    private Integer dacId;
    private Integer dacVer;
    private MethodePaiement methodePaiement;
    private Boolean statusPaiement;
    private Integer justifPaiementId;
}
