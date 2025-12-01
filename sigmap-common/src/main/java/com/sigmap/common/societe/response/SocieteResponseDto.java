package com.sigmap.common.societe.response;

import com.sigmap.common.activite.Activite;
import com.sigmap.common.activite.response.ActiviteResponseDtoSoft;
import com.sigmap.common.adresse.response.AdresseResponseDto;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.SocieteStatus;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SocieteResponseDto {
    private Integer id;
    private String nom;
    private RefParamResponseDto type;
    private RefParamResponseDto categorieSoumissionnaire;
    private String nif;
    private Date dateNif;
    private String numCnss;
    private SocieteStatus status;
    private boolean isBlackListed;
    private AdresseResponseDto address;
    private List<ActiviteResponseDtoSoft> activites;
}
