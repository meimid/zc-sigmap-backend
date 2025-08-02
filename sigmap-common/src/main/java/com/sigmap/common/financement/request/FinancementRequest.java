package com.sigmap.common.financement.request;

import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO for creating a Financement entity.
 * Contains all required fields for creating a new financing relationship
 * between a Realisation and a Monnaie.
 */
@Data
public class FinancementRequest {

    /**
     * The ID of the Realisation entity (part of composite key).
     */
    private Integer realisationId;

    /**
     * The version of the Realisation entity (part of composite key).
     */
    private Integer realisationVer;

    /**
     * The ID of the Monnaie entity (part of composite key).
     */
    private Integer monnaieId;

    /**
     * The ID of the financing source.
     */
    private Integer sourceDeFinancementId;

    /**
     * The estimated budget amount.
     */
    private BigDecimal budgetEstimer;
}
