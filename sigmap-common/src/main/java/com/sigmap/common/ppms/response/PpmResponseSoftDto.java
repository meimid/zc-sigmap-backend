package com.sigmap.common.ppms.response;

import com.sigmap.common.ac.response.AcResponseDto;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PpmResponseSoftDto {
    private Integer id;
    private Integer version;
    private String ref;
    private String nom;
    private String annee;
    private AcResponseDto ac;
    private BigDecimal budgetEstime;
    private String status;
    private RefParamResponseDto modePassation;
    private Boolean actualise;
    private Integer nbrActualisation;
    private String remarque;
}
