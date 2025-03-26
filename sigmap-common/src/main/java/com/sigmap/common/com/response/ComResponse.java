package com.sigmap.common.com.response;

import com.sigmap.common.ref_params.response.RefParamResponse;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

@Data
public class ComResponse {
    private Integer id;
    private String decretCreation;
    private String responsable;
    private Boolean special;
    private String ancrage;
    private String contact;
    private String adresse;
    private String membres;
    private RefParamResponse refParam;
    private BaseEntityResponse metadata;
}
