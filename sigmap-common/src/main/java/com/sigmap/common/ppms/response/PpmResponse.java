package com.sigmap.common.ppms.response;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PpmResponse {
    private Integer id;
    private Integer version;
    private String ref;
    private String nom;
    private String annee;
    private Ac ac;
    private BigDecimal budgetEstime;
    private RefParam status;
    private BaseEntityResponse metadata;
}