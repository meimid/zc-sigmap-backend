package com.sigmap.common.adresse.request;

import lombok.Data;

@Data
public class AdresseRequest {
    private String email;
    private String tel1;
    private String tel2;
    private String fax;
    private String adresse;
    private String compAdresse;
    private String codePostal;
    private String ville;
    private Integer paysId;
}
