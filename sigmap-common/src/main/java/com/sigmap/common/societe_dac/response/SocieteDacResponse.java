package com.sigmap.common.societe_dac.response;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.entity.MethodePaiement;
import com.sigmap.common.societe.Societe;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocieteDacResponse {
    //private DacForSocieteDacResponse dac;
    //private Dac dac;
    private Integer dacId;
    private Integer dacVer;
    private Integer societeId;
    private MethodePaiement methodePaiement;
    private Integer justifPaiementId;
    private Boolean statusPaiement;
}
