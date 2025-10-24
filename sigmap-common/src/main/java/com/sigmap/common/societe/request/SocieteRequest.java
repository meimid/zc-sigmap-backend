package com.sigmap.common.societe.request;

import com.sigmap.common.adresse.request.AdresseRequest;
import com.sigmap.common.entity.SocieteStatus;

import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SocieteRequest {
    private String nom;
    private Integer typeId;
    private Integer categorieSoumissionnaireId;
    private String nif;
    private Date dateNif;
    private String numCnss;
    private List<Integer> activiteIds=new ArrayList<>();
    private AdresseRequest adresseRequest;

}
