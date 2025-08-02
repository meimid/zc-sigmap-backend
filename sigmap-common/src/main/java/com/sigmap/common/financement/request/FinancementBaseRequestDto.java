package com.sigmap.common.financement.request;

import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO for updating a Financement entity.
 * All fields are optional to allow partial updates.
 * The composite key (realisationId, realisationVer, monnaieId) cannot be updated.
 */
@Data
public class FinancementBaseRequestDto {

    /**
     * The ID of the financing source.
     */
    private Integer sourceDeFinancementId;

    /**
     * The estimated budget amount.
     */
    private BigDecimal budgetEstimer;
}
