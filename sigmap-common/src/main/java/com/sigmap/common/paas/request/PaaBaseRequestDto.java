package com.sigmap.common.paas.request;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.realisations.request.RealisationRequestForPaa;
import com.sigmap.common.ref_params.RefParam;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Base DTO for PAA requests that support versioning.
 * This class is similar to PaaRequest but with support for document IDs and realisation relationships.
 */
@Data
public class PaaBaseRequestDto {
    private Integer id;
    private Integer version;
    private String ref;
    private String nom;
    private String annee;
    private Integer acId;
    private BigDecimal budgetEstime;
    private RefParam status;
    private RefParam modePassation;

    // IDs for many-to-many relationships
    private Set<CompositeId> realisationIds;

    private List<Integer> docIds;
} 