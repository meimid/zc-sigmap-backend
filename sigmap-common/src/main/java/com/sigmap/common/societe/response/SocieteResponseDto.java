package com.sigmap.common.societe.response;

import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.SocieteStatus;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.Data;

import java.util.Date;

@Data
public class SocieteResponseDto {
    private Integer id;
    private String nom;
    private RefParamResponseDto type;
    private RefParamResponseDto pays;
    private RefParamResponseDto categorieSoumissionnaire;
    private String nni;
    private String nif;
    private Date dateNif;
    private Integer numCnss;
    private SocieteStatus status;
}
