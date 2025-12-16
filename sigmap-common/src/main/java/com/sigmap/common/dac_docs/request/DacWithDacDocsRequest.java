package com.sigmap.common.dac_docs.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
@Data
public class DacWithDacDocsRequest {
    private Calendar dateLimiteSoumission;
    private Calendar dateLimiteRecptionDemande;
    private Boolean payant;
    private BigDecimal montantPayant;
    private String responsableDacId;
    private String conditionsParticipation;
    private String etapeDac;
    private Boolean shortListing;
    private List<Integer> shortlistMembers;
    List<DacDocsRequest> dacDocsRequests;
}
