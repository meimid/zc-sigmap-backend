package com.sigmap.common.financement.response;

import com.sigmap.common.source_financement.response.SourceFinancementResponseDto;
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
    private SourceFinancementResponseDto sourceFinancement;

    /**
     * The ID of the financing source.
     */
    private Integer monnaieId;

    /**
     * The estimated budget amount.
     */
    private BigDecimal budgetEstimer;

    private Boolean examenPrealable;

}
