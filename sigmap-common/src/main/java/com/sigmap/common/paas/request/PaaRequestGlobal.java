package com.sigmap.common.paas.request;


import com.sigmap.common.realisations.request.RealisationRequestForPaa;
import com.sigmap.common.ref_params.RefParam;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for creating a PAA with related entities in a single request.
 * Allows creation of PAA with its Realisations and Lots at the same time.
 */
@Data
public class PaaRequestGlobal {
    private String ref;
    private String nom;
    private String annee;
    private BigDecimal budgetEstime;
    private RefParam status;
    private RefParam modePassation;
    private String remarque;
    private Integer acId;
    private List<Integer> docIds;
    private List<RealisationRequestForPaa> realisations = new ArrayList<>();

    // toDO: nejah
    private List<MultipartFile> paaFiles;


} 