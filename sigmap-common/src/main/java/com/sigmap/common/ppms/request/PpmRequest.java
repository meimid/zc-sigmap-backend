package com.sigmap.common.ppms.request;

import com.sigmap.common.realisations.request.RealisationRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sun.istack.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PpmRequest {

    private Integer id;

    private Integer version;

    private String ref;

    private String nom;

    private String annee;

    private Integer acId;

    private BigDecimal budgetEstime;

    private RefParam status;

    private List<RealisationRequest> realisations;
}