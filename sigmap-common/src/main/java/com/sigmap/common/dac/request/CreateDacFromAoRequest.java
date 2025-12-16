package com.sigmap.common.dac.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateDacFromAoRequest {

    @NotNull(message = "Appel d'Offre ID is required")
    private Integer appelOffreId;

    // etapeDac is auto-determined from ModeDePassationSelection.etapes (ETAPE_UNIQUE or DEUX_ETAPES)

    @NotNull(message = "Date limite de soumission is required")
    private Calendar dateLimiteSoumission;

    @NotNull(message = "Date limite de réception des demandes is required")
    private Calendar dateLimiteRecptionDemande;

    @NotNull(message = "Payant field is required")
    private Boolean payant;

    private BigDecimal montantPayant;
    
    private Integer monnaieId; // Optional: Currency for payment (defaults to ID 1 if not provided)

    @NotNull(message = "Responsable DAC ID is required")
    private String responsableDacId;

    private String conditionsParticipation;

    private Boolean shortListing;

    private List<Integer> shortlistIds;

    private Integer dynaDocsModelId;

    private String etapeDac;
}
