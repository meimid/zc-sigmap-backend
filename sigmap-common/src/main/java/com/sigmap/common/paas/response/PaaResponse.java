package com.sigmap.common.paas.response;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.response.AcResponse;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PaaResponse {
    private Integer id;
    private Integer version;
    private String ref;
    private String nom;
    private String annee;
    //TODO:change it to AcResponse : Done
    private Ac ac;
    private BigDecimal budgetEstime;
    private String status;
    private RefParam modePassation;
    private String remarque;
    private List<Realisation> realisations;
    private String processInstanceId;
    private Boolean actualise;
    private Integer nbrActualisation;
    private BaseEntityResponse metadata;
}