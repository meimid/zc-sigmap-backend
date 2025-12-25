package com.sigmap.common.realisations.response;


import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.config.RealisationStatus;
import com.sigmap.common.financement.response.FinancementResponse;
import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.monnaie.response.MonnaieResponseDto;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class RealisationResponseForAO {
    private Integer id;
    private Integer version;
    private String realisationEnvisagees;
    private String beneficiaire;
    private List<FinancementResponse> financements;
    private RefParam imputationBudgetaire;
    private MonnaieResponseDto monnaie;
    private RefParam typeMarcher;
    private ModeDePassationSelection modePassation;
    private LocalDate datePrevLps;
    private LocalDate datePrevAc;
    private LocalDate datePrevDp;
    private LocalDate datePrevAp;
    private BigDecimal budgetEstime;
    private RealisationStatus statut;
    private Boolean isPublished;
    private Boolean publishedAt;
    private Integer sort;
    private List<Paa> paas;
    private BaseEntityResponse metadata;
}