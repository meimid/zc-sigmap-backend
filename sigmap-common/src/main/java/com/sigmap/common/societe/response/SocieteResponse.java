package com.sigmap.common.societe.response;

import com.sigmap.common.activite.Activite;
import com.sigmap.common.activite.response.ActiviteResponseDto;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.SocieteStatus;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SocieteResponse {
    private Integer id;
    private String nom;
    private RefParamResponseDto type;
    private RefParamResponseDto pays;
    private String nni;
    private RefParamResponseDto categorieSoumissionnaire;
    private String nif;
    private Date dateNif;
    private Integer numCnss;
    private SocieteStatus status;
    private List<ActiviteResponseDto> activites = new ArrayList<ActiviteResponseDto>();
    private BaseEntityResponse metadata;
}
