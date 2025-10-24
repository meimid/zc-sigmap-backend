package com.sigmap.common.societe.request;


import com.sigmap.common.adresse.request.AdresseRequest;
import com.sigmap.common.keycloak_website_admin.dto.UserResquest;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class SocieteRequestInscription {
    private String nom;
    private Integer typeId;
    private Integer categorieSoumissionnaireId;
    private String nif;
    private Date dateNif;
    private String numCnss;
    private List<Integer> activiteIds=new ArrayList<>();
    private AdresseRequest adresseRequest;
    private UserResquest userResquest;
}
