package com.sigmap.common.dac.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DacDetailsWebsiteResponse {
    private Long nbrJoursRestants;
    private Long nbrHeuresRestants;
    private String modePassationLibelle;
    private String modePassationType;
    private String nomAc;
    private String descriptionTypeMarche;
    private Calendar dateLimiteSoumission;
    private String alfrescoId;
    
    // AppelOffre details
    private String referenceAo;
    private String nomAppelOffre;
    private String descriptionAo;
    private Boolean lotSepares;
    
    // Realisation details
    private String financements;
    private List<String> lots;
    
    // DAC details
    private String referenceDac;
    private Calendar datePublicationDac;
    private String payant; // "Oui" / "Non"
    private String conditionsParticipation;
    
    private List<DacPrixWebsiteResponse> dacPrix;
    private ResponsableDacResponse responsableDac;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DacPrixWebsiteResponse {
        private String monnaieDescription;
        private String prix;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResponsableDacResponse {
        private String firstName;
        private String lastName;
        private String tel;
        private String email;
        private String adresse;
    }
}
