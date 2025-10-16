package com.sigmap.common.dac.request;

import lombok.Data;

import java.util.Calendar;

@Data
public class DacEditRequest {
    private Integer dacId;
    private Integer dacVer;
    private Calendar dateLimiteSoumission;
    private Calendar dateLimiteRecptionDemande;
    private boolean payant;
}
