package com.sigmap.common.realisations.response;

import com.sigmap.common.config.RealisationStatus;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.lots.response.LotResponse;
import com.sigmap.common.lots.response.LotResponseDTO;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class RealisationResponseDTO {
    private Integer id;
    private Integer version;
    private String realisationEnvisagees;
    private RefParamResponseDto service;
    private RefParamResponseDto direction;
    private RefParamResponseDto sourceFinancement;
    private RefParamResponseDto monais;
    private RefParamResponseDto typeMarcher;
    private RefParamResponseDto modeSelection;
    private LocalDate datePrevLps;
    private LocalDate datePrevAc;
    private LocalDate datePrevDp;
    private LocalDate datePrevAp;
    private BigDecimal budgetEstime;
    private RealisationStatus statut;
    private Boolean isPublished;
    private Boolean publishedAt;
    private Integer sort;
    private List<DocsResponseDTO> docs;
    private List<LotResponseDTO> lots;


} 