package com.sigmap.common.societe_dac.response;

import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.ref_params.RefParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Calendar;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DacForSocieteDacResponse {
    private Integer dacId;
    private Integer dacVer;
    private String reference;
    private Calendar dateLimiteSoumission;
    private Calendar dateLimiteRecptionDemande;
    private boolean payant;
    private boolean shortListing;
    private Integer parentDacId;
}
