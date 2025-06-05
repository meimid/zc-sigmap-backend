package com.sigmap.common.ppms.request;

import com.sigmap.common.lots.request.LotRequest;
import com.sigmap.common.realisations.request.RealisationBaseRequestDto;
import com.sigmap.common.ref_params.RefParam;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Data Transfer Object for PPM updates with versioning.
 * <p>
 * This DTO is used for updating PPM entities with versioning support. It contains all
 * fields that can trigger a version increment when changed. It also supports updating
 * nested Realisations with their own versioning logic.
 * <p>
 * Version increment logic:
 * - If any PPM field changes, a new PPM version is created
 * - If any Realisation changes/adds/deletes, a new PPM version is created
 * - If a Lot within a Realisation changes, both Realisation and PPM versions increment
 * - If any document is added/modified/removed, PPM version increments
 */
@Data
public class PpmBaseRequestDto {
    private String ref;
    private String nom;
    private String annee;
    private Integer acId;
    private BigDecimal budgetEstime;
    private RefParam status;
    private RefParam modePassation;
    // For updating existing or creating new realisations with versioning
    private List<RealisationBaseRequestDto> realisations;
    
    // For document management with versioning - simple IDs as documents don't use composite IDs
    private List<Integer> docIds;
    private Set<LotRequest> lots;
} 