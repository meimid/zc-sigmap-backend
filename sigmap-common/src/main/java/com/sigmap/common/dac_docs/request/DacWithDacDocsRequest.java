package com.sigmap.common.dac_docs.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Calendar;
import com.sigmap.common.dac_prix.request.DacPrixRequestForDac;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DacWithDacDocsRequest {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Calendar dateLimiteSoumission;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Calendar dateLimiteRecptionDemande;
    private Boolean payant;
    private List<DacPrixRequestForDac> dacPrixRequests;
    private String responsableDacId;
    private String conditionsParticipation;
    private String etapeDac;
    private Boolean shortListing;
    private List<Integer> shortlistMembers;
    List<DacDocsRequest> dacDocsRequests;
}
