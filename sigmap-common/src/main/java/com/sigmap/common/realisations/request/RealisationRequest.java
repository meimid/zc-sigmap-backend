package com.sigmap.common.realisations.request;

import com.sigmap.common.config.RealisationStatus;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.lots.response.LotResponse;
import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.ref_params.RefParam;
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
    private RefParam service;
    private RefParam direction;
    private RefParam sourceFinancement;
    private RefParam imputationBudgetaire;
    //    private RefParam monais;
    private Monnaie monnaie;
    private RefParam typeMarcher;
    private RefParam modeSelection;
    private LocalDate datePrevLps;
    private LocalDate datePrevAc;
    private LocalDate datePrevDp;
    private LocalDate datePrevAp;
    private BigDecimal budgetEstime;
    private RealisationStatus statut;
    private Boolean isPublished;
    private Boolean publishedAt;
    private Integer sort;

    // IDs for many-to-many relationships
    private List<CompositeId> ppmIds;
    private List<CompositeId> paaIds;
    private Set<CompositeId> lotIds;
    private List<Integer> docIds;
    private List<LotResponse> lots;

}
