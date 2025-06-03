package com.sigmap.common.paas.response;

import com.sigmap.common.ac.response.AcResponseDto;

import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaaResponseDtoWithMetdata {
    private Integer id;
    private Integer version;
    private String ref;
    private String nom;
    private String annee;
    private AcResponseDto ac;
    private BigDecimal budgetEstime;
    private RefParamResponseDto status;
    private String processInstanceId;
    private BaseEntityResponse metadata;
}
