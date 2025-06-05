package com.sigmap.common.paas.response;

import com.sigmap.common.ac.response.AcResponseDto;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaaResponseDto {
    private Integer id;
    private Integer version;
    private String ref;
    private String nom;
    private String annee;
    private AcResponseDto ac;
    private BigDecimal budgetEstime;
    private RefParamResponseDto status;
    private RefParam modePassation;
    private String processInstanceId;
}
