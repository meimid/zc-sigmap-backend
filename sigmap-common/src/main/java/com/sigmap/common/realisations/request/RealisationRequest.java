package com.sigmap.common.realisations.request;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.config.RealisationStatus;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.financement.request.FinancementBaseRequestDto;
import com.sigmap.common.financement.request.FinancementRequest;
import com.sigmap.common.financement.response.FinancementResponse;
import com.sigmap.common.lots.response.LotResponse;
import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.source_financement.SourceFinancement;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class RealisationRequest {

    private Integer id;
    private Integer version;
    private String realisationEnvisagees;
    private String beneficiaire;
    // private RefParam service;
    // private RefParam direction;
    private SourceFinancement sourceFinancement;
    private List<FinancementBaseRequestDto> financementRequests;
    private RefParam imputationBudgetaire;
    // private RefParam monais;
    private Monnaie monnaie;
    private RefParam typeMarcher;
    // private RefParam modeSelection;
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
    private String comment;

    // IDs for many-to-many relationships
    private List<CompositeId> ppmIds;
    private List<CompositeId> paaIds;
    private Set<CompositeId> lotIds;
    private List<Integer> docIds;
    private List<LotResponse> lots;

}
