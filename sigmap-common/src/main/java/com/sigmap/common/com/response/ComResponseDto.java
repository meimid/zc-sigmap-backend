package com.sigmap.common.com.response;

import com.sigmap.common.ref_params.response.RefParamResponse;
import lombok.Data;

@Data
public class ComResponseDto {
    private Integer id;
    private String nom;
    private String decretCreation;
    private String responsable;
    private Boolean special;
    private String ancrage;
    private String contact;
    private String adresse;
    private String membres;
    private RefParamResponse comType;
}
