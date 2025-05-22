package com.sigmap.common.realisations.response;

import com.sigmap.common.lots.response.LotResponse;
import com.sigmap.common.ref_params.RefParam;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class RealisationResponseDTO {
    private Integer id;
    private Integer version;
    private String realisationEnvisagees;
    private RefParam service;
    private RefParam direction;
    private RefParam sourceFinancement;
    private RefParam monais;
    private RefParam typeMarcher;
    private RefParam modeSelection;
    private LocalDate datePrevLps;
    private LocalDate datePrevAc;
    private LocalDate datePrevDp;
    private LocalDate datePrevAp;
    private BigDecimal budgetEstime;
    private Boolean isPublished;
    private Boolean publishedAt;
    private Integer sort;
    private Set<LotResponse> lots;
} 