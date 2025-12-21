package com.sigmap.common.societe_dac.response;

import com.sigmap.common.entity.SocieteStatus;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.Data;

import java.util.Date;

@Data
public class SocieteResponseForPaymentDTO {
    private Integer id;
    private String nom;
    private RefParamResponseDto type;
    private RefParamResponseDto categorieSoumissionnaire;
    private String nif;
    private Date dateNif;
    private String numCnss;
    private SocieteStatus status;
}
