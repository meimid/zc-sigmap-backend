package com.sigmap.common.activite.response;

import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.Data;

@Data
public class ActiviteResponseDto {
    private Integer id;
    private String libelle;
    private RefParamResponseDto categorieActivite;


}
