package com.sigmap.common.dac.request;

import com.sigmap.common.dac_prix.request.DacPrixRequest;
import com.sigmap.common.dac_prix.request.DacPrixRequestForDac;
import lombok.Data;

import java.util.Calendar;
import java.util.List;

@Data
public class DacEditRequest {
    private Integer dacId;
    private Integer dacVer;
    private Calendar dateLimiteSoumission;
    private Calendar dateLimiteRecptionDemande;
    private boolean payant;
    private List<DacPrixRequestForDac> dacPrixRequests;
}
