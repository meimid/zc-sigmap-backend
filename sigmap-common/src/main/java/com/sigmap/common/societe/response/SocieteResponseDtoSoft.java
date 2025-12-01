package com.sigmap.common.societe.response;

import lombok.Data;

@Data
public class SocieteResponseDtoSoft {
    private Integer id;
    private String nom;
    private String nif;
    private String email;
    private String responsableNom;
    private String responsablePrenom;
    private String responsableEmail;
    private String pays;
    private boolean isBlackListed;


}
