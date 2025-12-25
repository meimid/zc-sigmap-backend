package com.sigmap.common.dac_docs.request;

import com.sigmap.common.dac_doc_soumission.request.DacDocSoumissionCreateRequest;
import com.sigmap.common.dac_dyna_form.request.DacDynaFormRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
    private List<DacDynaFormRequest> dacDynaFormRequest;
    private List<DacDocSoumissionCreateRequest> dacDocSoumissionCreateRequests;
    private String responsableDacId;
    private String conditionsParticipation;
    private List<Integer> shortlistMembers;
    private List<DacDocsRequest> dacDocsRequests;
}
