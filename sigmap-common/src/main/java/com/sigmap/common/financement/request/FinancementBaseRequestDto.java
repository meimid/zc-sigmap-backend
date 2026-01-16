package com.sigmap.common.financement.request;

import com.sigmap.common.source_financement.SourceFinancement;
import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO for updating a Financement entity.
 * All fields are optional to allow partial updates.
 * The composite key (realisationId, realisationVer, monnaieId) cannot be updated.
 */
@Data
public class FinancementBaseRequestDto {

    private Integer monnaieId;

    /**
     * The ID of the financing source.
     */
    private SourceFinancement sourceFinancement;

    /**
     * The estimated budget amount.
     */
    private BigDecimal budgetEstimer;

    private Boolean examenPrealable;
}
