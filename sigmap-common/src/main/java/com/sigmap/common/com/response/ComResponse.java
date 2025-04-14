package com.sigmap.common.com.response;

import com.sigmap.common.ac.response.AcResponseDto;
import com.sigmap.common.ref_params.response.RefParamResponse;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class ComResponse {
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
    private List<AcResponseDto> acs;
    private BaseEntityResponse metadata;
}
