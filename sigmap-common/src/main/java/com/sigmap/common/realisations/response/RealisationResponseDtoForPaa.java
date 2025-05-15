package com.sigmap.common.realisations.response;

import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.lots.response.LotPaaResponse;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A specialized Realisation response DTO for PAA responses.
 * Contains simplified lot information and doesn't include PAA references
 * to avoid circular references.
 */
@Data
public class RealisationResponseDtoForPaa {
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
    private List<DocsResponseDTO> docs;
    // Using simplified lot responses and removing paas reference to avoid circular references
    private List<LotPaaResponse> lots;
    private BaseEntityResponse metadata;
} 