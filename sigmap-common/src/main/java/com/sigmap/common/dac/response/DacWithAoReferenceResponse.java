package com.sigmap.common.dac.response;

import com.sigmap.common.entity.ModePassation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DacWithAoReferenceResponse {

    // DAC fields
    private Integer dacId;
    private Integer dacVer;
    private String reference;
    private String status;
    private Calendar dateLimiteSoumission;
    private Calendar dateLimiteRecptionDemande;
    private Boolean payant;
    private BigDecimal montantPayant;
    private String responsableDacId;
    private String conditionsParticipation;
    private String etapeDac;
    private Boolean shortListing;
    private Integer parentDacId;

    // Reference data from AO
    private AoReferenceData aoReference;

    // Reference data from PPM
    private PpmReferenceData ppmReference;

    // Reference data from Realisation
    private RealisationReferenceData realisationReference;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AoReferenceData {
        private Integer aoId;
        private String aoReference;
        private String aoNom;
        private String aoDescription;
        private String etape;
        private Boolean lotSepares;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PpmReferenceData {
        private Integer ppmId;
        private Integer ppmVer;
        private String ppmRef;
        private String ppmNom;
        private String annee;
        private String acNom;
        private Integer acId;
        private LocalDate datePublication;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RealisationReferenceData {
        private Integer realisationId;
        private Integer realisationVer;
        private String realisationEnvisagees;
        private String typeMarcher;
        private String typeMarcherLibelle;
        private String modePassation;
        private String modePassationLibelle;
        private Boolean isInternational;
        private List<FinancementData> sourcesFinancement;
        private List<LotData> lots;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FinancementData {
        private String sourceFinancement;
        private BigDecimal budgetEstimer;
        private String monnaie;
        private Double tauxApplicable;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LotData {
        private Integer lotId;
        private Integer lotVer;
        private String description;
        private BigDecimal budget;
    }
}
