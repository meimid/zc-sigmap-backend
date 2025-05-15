package com.sigmap.common.paas.response;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.realisations.response.RealisationResponseDtoForPaa;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * An enhanced PAA response DTO that includes specialized Realisation DTOs
 * to avoid circular references and provide a more targeted view of the data.
 */
@Data
public class PaaResponseWithRealisations {
    private Integer id;
    private Integer version;
    private String ref;
    private String nom;
    private String annee;
    private Ac ac;
    private BigDecimal budgetEstime;
    private RefParam status;
    private List<DocsResponseDTO> docs;
    // Using the specialized Realisation DTO to avoid circular references
    private List<RealisationResponseDtoForPaa> realisations;

    private BaseEntityResponse metadata;

} 