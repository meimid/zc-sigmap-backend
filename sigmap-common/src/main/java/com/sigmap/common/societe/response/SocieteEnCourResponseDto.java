package com.sigmap.common.societe.response;

import com.sigmap.common.activite.response.ActiviteResponseDto;
import com.sigmap.common.adresse.response.AdresseResponseDto;
import com.sigmap.common.entity.SocieteStatus;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.user_societe.response.UserSocieteResponseDto;
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
public class SocieteEnCourResponseDto {
    private Integer id;
    private String nom;
    private RefParamResponseDto type;
    private RefParamResponseDto categorieSoumissionnaire;
    private String nif;
    private Date dateNif;
    private String numCnss;
    private SocieteStatus status;
    private boolean isBlackListed;
    private  AdresseResponseDto address  ;
    private List<ActiviteResponseDto> activites = new ArrayList<>();
    private List<UserSocieteResponseDto> usersSociete = new ArrayList<>();
}
