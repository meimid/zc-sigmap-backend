package com.sigmap.common.financement.response;

import com.sigmap.common.ref_params.RefParam;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinancementResponse {


    /**
     * The unique financement identifier (part of composite key).
     */
    private Integer financementId;

    /**
     * The ID of the associated Realisation entity.
     */
    private Integer realisationId;

    /**
     * The version of the associated Realisation entity.
     */
    private Integer realisationVer;

    /**
     * The ID of the financing source.
     */
    private RefParam sourceFinancement;

    /**
     * The ID of the financing source.
     */
    private Integer monnaieId;

    /**
     * The estimated budget amount.
     */
    private BigDecimal budgetEstimer;




}
